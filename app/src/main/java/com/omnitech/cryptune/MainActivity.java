package com.omnitech.cryptune;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.omnitech.cryptune.Adapter.LoginPanelViewAdapter;
import com.omnitech.cryptune.ChatsPackage.ChatsHome;
import com.omnitech.cryptune.Fragments.Login;
import com.omnitech.cryptune.Fragments.Register;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String PRIVATEKEY = "PRIVATEKEY";
    private static final String phNo = "phNo";
    private static final String NAME = "NAME";
    private static final String PUBLICKEY = "PUBLICKEY";
    private static final String FCM = "FCM";
    private static final int ERROR_DIALOGUE_REQUEST = 102;

    private ProgressBar progressBar;
    private ViewPager viewPager;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //after applying tabLayout
        Toolbar toolbar = findViewById(R.id.toolbar_activity_main);
        TabLayout tabLayout = findViewById(R.id.tabs_main_activity);

        viewPager = findViewById(R.id.view_pager_activity_main);
        progressBar = findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        viewPager.setAdapter(setupViewPager());
        tabLayout.setupWithViewPager(viewPager);

        firebaseAuth = FirebaseAuth.getInstance();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                onPause();
                CheckConnectivity();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        CheckConnectivity();
        authStateListener = firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified() && isServicesOK()) {
                String email = firebaseAuth.getCurrentUser().getEmail();
                FirebaseFirestore.getInstance().collection("Users").whereEqualTo("Email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            boolean exists = !task.getResult().getDocuments().isEmpty();
                            if (exists) {
                                Intent intent = new Intent(MainActivity.this, ChatsHome.class);
                                startActivity(intent);
                                MainActivity.this.finish();
                            }
                        }
                    }
                });
            }
        };
    }

    private LoginPanelViewAdapter setupViewPager() {
        LoginPanelViewAdapter panel = new LoginPanelViewAdapter(getSupportFragmentManager());
        panel.addFragment(new Login(), "Login");
        panel.addFragment(new Register(), "Register");
        return panel;
    }

    public void SignInUser(final String email, final String pass) {
        onPause();
        showProgress();
        firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    hideProgress();
                    Login frag = (Login) getSupportFragmentManager().getFragments().get(0);
                    Objects.requireNonNull(frag).show_forgot_pass();
                    if (task.getException() != null) {
                        Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                fetchUserData(email);
                            }
                        });
                    } else if (!firebaseAuth.getCurrentUser().isEmailVerified()) {
                        hideProgress();
                        firebaseAuth.signOut();
                        Toast.makeText(MainActivity.this, "Please verify email address", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    private void fetchUserData(final String email) {
        //get the doc in Users where the Email Field matches the entered email

        FirebaseFirestore.getInstance().collection("Users").whereEqualTo("Email", email).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                firebaseAuth.signOut();
                hideProgress();
                Toast.makeText(MainActivity.this, "Something Went Wrong, Try again", Toast.LENGTH_SHORT).show();
            } else {
                final QuerySnapshot querySnapshot = task.getResult();

                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    FirebaseMessaging.getInstance().setAutoInitEnabled(true);
                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                        @Override
                        public void onSuccess(InstanceIdResult instanceIdResult) {
                            DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                            String phone = doc.getId();
                            FirebaseFirestore.getInstance().collection("Users").document(phone).update("FCM", instanceIdResult.getToken()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    final SharedPreferences sharedPreferences = androidx.preference.PreferenceManager
                                            .getDefaultSharedPreferences(MainActivity.this);
                                    final SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(NAME, doc.getString("Name"));
                                    editor.putString(phNo, phone);
                                    editor.putString(PRIVATEKEY, doc.getString("PVK"));
                                    editor.putString(PUBLICKEY, doc.getString("PUK"));
                                    editor.putString(FCM, doc.getString("FCM"));
                                    editor.apply();

                                    if (task.getException() != null) {
                                        hideProgress();
                                        Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    } else if (isServicesOK()) {
                                        hideProgress();
                                        startActivity(new Intent(MainActivity.this, ChatsHome.class));
                                    }
                                }
                            });
                        }
                    });

                } else {
                    hideProgress();
                    showInvalidUserMessage();
                }
            }
        });
    }

    public boolean CheckConnectivity() {
        boolean netState;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        netState = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        if (!netState) {
            Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView().findViewById(R.id.main_root), "No Internet", Snackbar.LENGTH_LONG)
                    .setAction("CLOSE", view1 -> {

                    })
                    .setActionTextColor(Color.parseColor("#FFDF5050"));
            snackbar.show();
            hideProgress();
        }
        return netState;
    }

    public boolean isServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and user can make map request
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOGUE_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "you cant make map request", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void showProgress() {
        Login frag = (Login) getSupportFragmentManager().getFragments().get(0);
        Objects.requireNonNull(frag).disableLogin();
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        if (!getSupportFragmentManager().getFragments().isEmpty()) {
            int fragments = getSupportFragmentManager().getFragments().size();
            Login frag = (Login) getSupportFragmentManager().getFragments().get(0);
            Objects.requireNonNull(frag).enableLogin();
        }
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void hide() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        }
    }

    private void showDialogue(String title, String body) {
        final View dialogView = View.inflate(this, R.layout.custom_dialogue, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        final TextView titleText = dialogView.findViewById(R.id.title);
        final TextView bodyText = dialogView.findViewById(R.id.body);
        final Button confirmBtn = dialogView.findViewById(R.id.confirm);
        final Button cancelBtn = dialogView.findViewById(R.id.cancel);

        titleText.setText(title);
        bodyText.setText(body);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                MainActivity.this.finishAndRemoveTask();
                MainActivity.this.finishAffinity();
                System.exit(0);
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setView(dialogView);
        alertDialog.show();
    }

    private void showInvalidUserMessage() {
        showProgress();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseAuth.signOut();
                hideProgress();
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }
        }, 2000);
    }

    @Override
    public void onBackPressed() {
        showDialogue("Exit Application", "Are you sure you want to exit CrypTune?");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() == null || firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().getUid().isEmpty()) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        } else {
            firebaseAuth.addAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }
}