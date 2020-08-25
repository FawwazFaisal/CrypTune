package com.omnitech.cryptune.SendNotificationPack;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseIdService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                if (firebaseUser != null) {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyFirebaseIdService.this);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("FCMToken", instanceIdResult.getToken());
                    editor.apply();

                    String doc = sharedPreferences.getString("EMAIL", "");

                    FirebaseFirestore.getInstance().collection("Users").document(doc).update("FCMToken", instanceIdResult.getToken());
                }
            }
        });
    }
}
