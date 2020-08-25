package com.omnitech.cryptune.ChatsPackage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.omnitech.cryptune.MainActivity;
import com.omnitech.cryptune.MessagesPackage.Messages;
import com.omnitech.cryptune.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class ChatsHome extends AppCompatActivity {

    public static final String phNo = "phNo";
    RecyclerView recyclerView;
    ChatListAdapter adapter;
    DrawerLayout drawer;
    FloatingActionButton addContact;

    TextView nothingfound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_home);

        drawer = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        nothingfound = findViewById(R.id.nothingfound);
        addContact = findViewById(R.id.addContact);
        recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //get list of contacts
        SharedPreferences sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        String myPhone = sharedPreferences.getString(phNo, "");

        //get list of relevant conversations in messages
        if (CheckConnection()) {
            getChats(myPhone);
        }
        addContact.setOnClickListener(v -> {
            View alertView = View.inflate(this, R.layout.add_contact, null);
            AlertDialog dialog = new AlertDialog.Builder(this).create();
            dialog.setCanceledOnTouchOutside(false);

            ProgressBar progressBar = alertView.findViewById(R.id.progressBar2);

            TextInputLayout Name = alertView.findViewById(R.id.Name);
            TextInputLayout Number = alertView.findViewById(R.id.Number);

            Button confirm = alertView.findViewById(R.id.confirm);
            Button cancel = alertView.findViewById(R.id.cancel);

            confirm.setOnClickListener(l -> {
                progressBar.setVisibility(View.VISIBLE);

                int sizeName = Name.getEditText().getText().toString().length();
                int sizeNumber = Number.getEditText().getText().toString().length();

                String name = Name.getEditText().getText().toString();
                String number = Number.getEditText().getText().toString();

                confirm.setEnabled(false);
                confirm.setClickable(false);
                if (sizeName <= 20 && sizeName > 0 && sizeNumber == 11) {
                    FirebaseFirestore.getInstance().collection("Users").document(number).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                if (!task.getResult().exists()) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(ChatsHome.this, "No user found", Toast.LENGTH_SHORT).show();
                                } else {
                                    DocumentSnapshot doc = task.getResult();
                                    String PUK = doc.getString("PUK");
                                    String FCM = doc.getString("FCM");
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("PUK", PUK);
                                    map.put("FCM", FCM);
                                    map.put("Name", name);
                                    FirebaseFirestore.getInstance().collection("Users").document(myPhone).collection("Messages").document(number).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ChatsHome.this, "Contact created Successfully", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(ChatsHome.this, ChatsHome.class));
                                            } else {
                                                Toast.makeText(ChatsHome.this, "Error adding contact", Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
                }
            });
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.setView(alertView);
            dialog.show();
        });
    }

    private void getChats(String myPhone) {

        FirebaseFirestore.getInstance().collection("Users").document(myPhone).collection("Messages").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<ChatObject> chats = new ArrayList<>();
                if (task.isSuccessful() && task.getResult() != null) {
                    QuerySnapshot snapshots = task.getResult();
                    for (DocumentSnapshot doc : snapshots) {
                        ChatObject chat = new ChatObject(doc.getString("Name"), doc.getString("FCM"), doc.getString("PUK"), doc.getId());
                        chats.add(chat);
                    }
                    adapter = new ChatListAdapter(ChatsHome.this, R.layout.chat_row, chats);
                    adapter.setOnItemClickListener(position ->
                            {
                                String chatSelected = chats.get(position).getPhone();
                                String FCM = chats.get(position).getFCM();
                                String PUK = chats.get(position).getPUK();
                                String Name = chats.get(position).getName();
                                Intent intent = new Intent(ChatsHome.this, Messages.class);
                                intent.putExtra("Name", Name);
                                intent.putExtra("ID", chatSelected);
                                intent.putExtra("FCM", FCM);
                                intent.putExtra("PUK", PUK);
                                startActivity(intent);
                            }
                    );
                    recyclerView.setAdapter(adapter);
                } else {
                    if (chats.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                    }
                    Toast.makeText(ChatsHome.this, "Nothing found", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    public void Logout() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId();
                    FirebaseInstanceId.getInstance().getInstanceId();
                } catch (final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ChatsHome.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }.start();
        FirebaseMessaging.getInstance().setAutoInitEnabled(false);
        FirebaseAuth.getInstance().signOut();
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(ChatsHome.this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        startActivity(new Intent(ChatsHome.this, MainActivity.class));
        ChatsHome.this.finish();
    }

    @Override
    public void onBackPressed() {
        View alertView = View.inflate(this, R.layout.custom_dialogue, null);
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        Button cancel = alertView.findViewById(R.id.cancel);
        TextView title, message;
        title = alertView.findViewById(R.id.title);
        message = alertView.findViewById(R.id.body);
        title.setText("EXIT APPLICATION");
        message.setText("Are you sure yo want to exit?");

        cancel.setText("Logout NOW!");
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Logout();
            }
        });
        Button confirm = alertView.findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ChatsHome.this.finishAndRemoveTask();
                ChatsHome.this.finishAffinity();
                System.exit(0);
            }
        });

        dialog.setView(alertView);
        dialog.show();
    }
}