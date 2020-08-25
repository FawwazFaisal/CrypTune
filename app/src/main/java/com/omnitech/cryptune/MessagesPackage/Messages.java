package com.omnitech.cryptune.MessagesPackage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.omnitech.cryptune.R;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import javax.crypto.Cipher;

public class Messages extends AppCompatActivity {
    public static final String phNo = "phNo";
    private static final String NAME = "NAME";
    private static final String PUBLICKEY = "PUBLICKEY";
    private static final String PRIVATEKEY = "PRIVATEKEY";
    private static final String FCM = "FCM";
    RecyclerView recyclerView;
    MessageListAdapter adapter;
    TextInputLayout msgBox;
    FloatingActionButton sendBtn;
    DrawerLayout drawer;
    String myFCM, myPUK, myName, myPhone, myPVK;
    String OFCM, OPUK, OName, OPhone;
    ArrayList<MessageObject> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        drawer = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        NestedScrollView scrollView = findViewById(R.id.scrollViewMessages);
        scrollView.setNestedScrollingEnabled(false);
        recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        msgBox = findViewById(R.id.msgBox);
        sendBtn = findViewById(R.id.sendBtn);

        msgBox.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (msgBox.getEditText().getText().length() == 0) {
                    sendBtn.setEnabled(true);
                    sendBtn.setClickable(true);
                    sendBtn.startAnimation(AnimationUtils.loadAnimation(Messages.this, R.anim.fadein));
                    sendBtn.setImageResource(R.drawable.send_message);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (msgBox.getEditText().getText().length() == 0) {
                    sendBtn.setEnabled(false);
                    sendBtn.setClickable(false);
                    sendBtn.setImageResource(R.drawable.send_message_disabled);
                    sendBtn.startAnimation(AnimationUtils.loadAnimation(Messages.this, R.anim.fadein));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Intent callerIntent = getIntent();
        //to read and decrypt messages
        OPhone = callerIntent.getStringExtra("ID");
        OFCM = callerIntent.getStringExtra("FCM");
        OPUK = callerIntent.getStringExtra("PUK");
        OName = callerIntent.getStringExtra("Name");

        //to encrypt and send messages
        SharedPreferences sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        myPhone = sharedPreferences.getString(phNo, "");
        myPUK = sharedPreferences.getString(PUBLICKEY, "");
        myFCM = sharedPreferences.getString(FCM, "");
        myName = sharedPreferences.getString(NAME, "");
        myPVK = sharedPreferences.getString(PRIVATEKEY, "");

        if (CheckConnection()) {
            //get list of relevant conversations in messages
            getMessages(myPhone, OPhone);

            sendBtn.setOnClickListener(v -> {
                String message = Objects.requireNonNull(msgBox.getEditText()).getText().toString();
                if (message.length() <= 250 && message.length() > 0) {
                    msgBox.getEditText().getText().clear();
                    encrypt(message, myPVK);
                }
            });
        }
    }

    private void encrypt(String message, String PVK) {
        //do encryption

        String encrypted_message = "";
        try {
            //deserialize serialized PVK
            KeyFactory RSAKeyFactory = KeyFactory.getInstance("RSA");
            byte[] pvk = Base64.decode(PVK, Base64.DEFAULT);
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(pvk);
            PrivateKey privateKey = RSAKeyFactory.generatePrivate(pkcs8EncodedKeySpec);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            encrypted_message = Base64.encodeToString(cipher.doFinal(message.getBytes()), Base64.DEFAULT);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        updateChats(encrypted_message);
    }

    private void updateChats(String message) {
        HashMap<String, String> messageMap = new HashMap<>();
        messageMap.put("Text", message);
        messageMap.put("Phone", myPhone);
        HashMap<String, String> myMetaRefForOtherMap = new HashMap<>();
        myMetaRefForOtherMap.put("PUK", myPUK);
        myMetaRefForOtherMap.put("Name", myName);
        myMetaRefForOtherMap.put("FCM", myFCM);
        //add message to this user's messages
        FirebaseFirestore.getInstance().collection("Users").document(myPhone)
                .collection("Messages").document(OPhone)
                .collection("Messages").document(String.valueOf(Calendar.getInstance().getTimeInMillis())).set(messageMap).addOnSuccessListener(aVoid -> {
            //document of chat whose metadata is to be updated
            DocumentReference chatDocRef = FirebaseFirestore.getInstance().collection("Users").document(OPhone).collection("Messages").document(myPhone);
            chatDocRef.set(myMetaRefForOtherMap, SetOptions.merge());
            //add to the collection of messages to recipients' Message collection inside chat
            CollectionReference msgColRef = chatDocRef.collection("Messages");
            //add messages to other user's collection
            msgColRef.document(String.valueOf(Calendar.getInstance().getTimeInMillis())).set(messageMap).addOnSuccessListener(aVoid1 -> {
                MessageObject text = new MessageObject(myPhone, message, String.valueOf(Calendar.getInstance().getTimeInMillis()));
                messages.add(text);
                adapter.notifyItemInserted(messages.size() - 1);
                recyclerView.scrollToPosition(messages.size() - 1);
                recyclerView.smoothScrollToPosition(Objects.requireNonNull(recyclerView.getAdapter()).getItemCount() - 1);
            });
        });
    }

    private void getMessages(String myPhone, String chatID) {
        messages = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("Users")
                .document(myPhone).collection("Messages").document(chatID).collection("Messages").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DocumentSnapshot doc : task.getResult()) {
                    String timestamp = doc.getId();
                    String text = doc.getString("Text");
                    String phoneOfText = doc.getString("Phone");
                    messages.add(new MessageObject(phoneOfText, text, timestamp));
                }
                adapter = new MessageListAdapter(Messages.this, R.layout.message_row, messages);
                adapter.setOnItemClickListener(position -> {
                    View itemView = recyclerView.findViewHolderForAdapterPosition(position).itemView;
                    TextView Received = itemView.findViewById(R.id.Received);
                    TextView Sent = itemView.findViewById(R.id.Sent);
                    String PUK = "";
                    if (messages.get(position).getPhone().equals(myPhone)) {
                        PUK = myPUK;
                        Sent.setText(decrypt(messages.get(position).getText(), PUK));
                        Sent.setEnabled(false);
                        Sent.setClickable(false);
                    } else if (messages.get(position).getPhone().equals(OPhone)) {
                        PUK = OPUK;
                        Received.setText(decrypt(messages.get(position).getText(), PUK));
                        Received.setEnabled(false);
                        Received.setClickable(false);
                    }
                });
                recyclerView.setAdapter(adapter);
                recyclerView.scrollToPosition(messages.size() - 1);
            } else {
                if (messages.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    Toast.makeText(this, "No messages", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(Messages.this, "Nothing found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String decrypt(String cypher, String PUK) {
        String decryptedString = "";
        try {
            //deserialize public key
            KeyFactory RSAKeyFactory = KeyFactory.getInstance("RSA");
            byte[] puk = Base64.decode(PUK, Base64.DEFAULT);
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(puk);
            PublicKey publicKey = RSAKeyFactory.generatePublic(x509EncodedKeySpec);

            // get an RSA cipher object and print the provider
            Cipher cipher = Cipher.getInstance("RSA");
            // encrypt the plain text using the public key
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.decode(cypher, Base64.DEFAULT));
            decryptedString = new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedString;
    }

    private boolean CheckConnection() {
        boolean netState;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        netState = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        if (!netState) {
            Snackbar.make(drawer, "No Internet", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    })
                    .setActionTextColor(Color.parseColor("#FFDF5050"))
                    .show();
        }
        return netState;
    }
}