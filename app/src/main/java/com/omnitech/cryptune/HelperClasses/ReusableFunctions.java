package com.omnitech.cryptune.HelperClasses;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.omnitech.cryptune.R;
import com.omnitech.cryptune.SendNotificationPack.APIService;
import com.omnitech.cryptune.SendNotificationPack.Client;
import com.omnitech.cryptune.SendNotificationPack.MyResponse;
import com.omnitech.cryptune.SendNotificationPack.NotificationSender;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReusableFunctions {
    AppCompatActivity activity;
    AlertDialog alertDialog;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    APIService apiService;

    public ReusableFunctions(Context context, AppCompatActivity activity) {
        this.activity = activity;
        sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
    }

    public Button[] showDialogue(String title, String body, ViewGroup parent) {
        final View dialogView = LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.custom_dialogue, parent, false);
        alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setCanceledOnTouchOutside(false);

        final TextView titleText = dialogView.findViewById(R.id.title);
        final TextView bodyText = dialogView.findViewById(R.id.body);
        final Button confirmBtn = dialogView.findViewById(R.id.confirm);
        final Button cancelBtn = dialogView.findViewById(R.id.cancel);

        titleText.setText(title);
        bodyText.setText(body);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAlertDialog();
            }
        });
        alertDialog.setView(dialogView);
        alertDialog.show();

        Button[] btnArray = new Button[]{confirmBtn, cancelBtn};

        return btnArray;
    }

    public void dismissAlertDialog() {
        alertDialog.dismiss();
    }


    public void sendNotification(@NonNull final NotificationSender body) {
        final String email = body.to;
        //email of the receiver
        saveToMessages(body, email);
        FirebaseFirestore.getInstance().collection("Users").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot doc = task.getResult();
                if (doc != null && doc.exists()) {
                    String userToken = doc.getString("FCMToken");
                    body.setTo(userToken);
                    apiService.sendNotifcation(body).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                            if (response.code() == 200) {
                                if (response.body() != null && response.body().success == 1) {
                                    Toast.makeText(activity, "Notified", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(activity, response.message(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {
                            Toast.makeText(activity, "Failed " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(activity, "Token Invalid", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveToMessages(final NotificationSender body, String email) {
        //3 writes
        //1 read
        final String date = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(new Date());
        final DocumentReference ID = FirebaseFirestore.getInstance().collection("Users").document(email);
        ID.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot doc = task.getResult();
                int notificationsID = Integer.parseInt(Objects.requireNonNull(doc.get("TotalMessages")).toString());

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("title", body.getData().getName());
                hashMap.put("message", body.getData().getMessage());
                hashMap.put("date", date);
                ID.collection("Messages").document(String.valueOf(notificationsID)).set(hashMap);
                ID.update("TotalNotifications", FieldValue.increment(1));
                ID.update("Unread", FieldValue.increment(1));
            }
        });
    }
}
