package com.omnitech.cryptune.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.omnitech.cryptune.MainActivity;
import com.omnitech.cryptune.R;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Register extends Fragment {
    KeyPairGenerator kpg;
    KeyPair kp;
    String PUK;
    String PVK;
    private TextInputLayout Email, Password, confirmPassword, Username, Phno, SMSCode;
    private String mVerificationId, smscode, usrname, phone, email, pass;
    private Button register, verify;
    private FirebaseAuth firebaseAuth;
    private boolean isNewUser;
    private View registerFragment;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.register) {
                ((MainActivity) requireActivity()).hide();
                processEmail();
            } else if (v.getId() == R.id.verify) {
                ((MainActivity) requireActivity()).hide();
                if (TextUtils.isEmpty(Objects.requireNonNull(SMSCode.getEditText()).getText().toString().trim()) || SMSCode.getEditText().getText().toString().trim().length() != 6) {
                    SMSCode.setError("Invalid");
                    SMSCode.requestFocus();
                } else {
                    smscode = SMSCode.getEditText().getText().toString();
                    verifyCodes(smscode);
                }
            }
        }
    };

    public static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        registerFragment = inflater.inflate(R.layout.fragment_register, container, false);
        registerFragment.setOnClickListener(v -> ((MainActivity) requireActivity()).hide());
        firebaseAuth = FirebaseAuth.getInstance();

        initViews();
        Connectivity();
        register.setOnClickListener(onClickListener);
        verify.setOnClickListener(onClickListener);
        return registerFragment;
    }

    private void initViews() {
        Username = registerFragment.findViewById(R.id.Username);
        Email = registerFragment.findViewById(R.id.Email);
        Password = registerFragment.findViewById((R.id.Password));
        confirmPassword = registerFragment.findViewById((R.id.ConfPassword));
        Phno = registerFragment.findViewById(R.id.PhNo);
        SMSCode = registerFragment.findViewById(R.id.SMSCode);
        register = registerFragment.findViewById(R.id.register);
        verify = registerFragment.findViewById(R.id.verify);
        textChangeListeners();
    }

    private void textChangeListeners() {
        Objects.requireNonNull(Email.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Email.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        Objects.requireNonNull(Password.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Password.setError(null);
                if (TextUtils.equals(s, Objects.requireNonNull(confirmPassword.getEditText()).getText())) {
                    confirmPassword.getEditText().setTextColor(Color.parseColor("#FFFFFF"));
                    Password.getEditText().setTextColor(Color.parseColor("#FFFFFF"));
                } else {
                    Password.getEditText().setTextColor(Color.parseColor("#F36262"));
                    confirmPassword.getEditText().setTextColor(Color.parseColor("#F36262"));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        Objects.requireNonNull(confirmPassword.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmPassword.setError(null);
                if (!TextUtils.equals(s, Objects.requireNonNull(Password.getEditText()).getText())) {
                    Password.getEditText().setTextColor(Color.parseColor("#F36262"));
                    confirmPassword.getEditText().setTextColor(Color.parseColor("#F36262"));
                } else {
                    confirmPassword.getEditText().setTextColor(Color.parseColor("#FFFFFF"));
                    Password.getEditText().setTextColor(Color.parseColor("#FFFFFF"));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        Objects.requireNonNull(Username.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Username.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        Objects.requireNonNull(Phno.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Phno.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        Objects.requireNonNull(SMSCode.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SMSCode.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public boolean Connectivity() {
        return ((MainActivity) requireActivity()).CheckConnectivity();
    }

    private void showProgress(Button btn) {
        ((MainActivity) requireActivity()).showProgress();
        disableBtn(btn);
    }

    private void hideProgress(Button btn) {
        ((MainActivity) requireActivity()).hideProgress();
        enableBtn(btn);
    }

    public void enableBtn(Button btn) {
        btn.setTextColor(Color.parseColor("#FFFFFF"));
        btn.setEnabled(true);
        btn.setClickable(true);
    }

    public void disableBtn(final Button btn) {
        btn.setTextColor(Color.rgb(145, 145, 145));
        btn.setEnabled(false);
        btn.setClickable(false);
    }

    public void processEmail() {
        email = Objects.requireNonNull(Email.getEditText()).getText().toString().trim();
        pass = Objects.requireNonNull(Password.getEditText()).getText().toString().trim();
        final String conPass = Objects.requireNonNull(confirmPassword.getEditText()).getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Email.setError("Missing");
        } else if (!isValidEmail(email)) {
            Email.setError("Invalid");
        } else if (TextUtils.isEmpty(pass)) {
            Password.setError("Missing");
        } else if (TextUtils.isEmpty(conPass)) {
            confirmPassword.setError("Missing");
        } else if (pass.length() < 8) {
            Password.setError("Too weak");
        } else if (!TextUtils.equals(conPass, pass)) {
            confirmPassword.setError("Password mismatched");
        } else {
            if (Connectivity()) {
                showProgress(register);
                firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        hideProgress(register);
                        if (task.getException() != null) {
                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        isNewUser = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getSignInMethods()).isEmpty();
                        if (isNewUser) {
                            processPhoneNo();
                        } else {
                            hideProgress(register);
                            Snackbar.make(requireActivity().getWindow().getDecorView().getRootView().findViewById((R.id.main_root)), "Email Already exists", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Sign In", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            startActivity(new Intent(getActivity(), MainActivity.class));
                                        }
                                    })
                                    .setActionTextColor(Color.parseColor("#FFDF5050"))
                                    .show();
                        }
                    }
                });
            }
        }

    }

    private void processPhoneNo() {
        usrname = Objects.requireNonNull(Username.getEditText()).getText().toString().trim();
        phone = Objects.requireNonNull(Phno.getEditText()).getText().toString().trim();

        if (TextUtils.isEmpty(usrname)) {
            Username.setError("Please Enter Username");
            Username.requestFocus();
        } else if (TextUtils.isEmpty(phone)) {
            Phno.setError("Enter Phone no.");
        } else if (phone.length() != 10) {
            Phno.setError("Enter Correct Phone no.");
            Phno.requestFocus();
        } else {
            if (Connectivity()) {
                showProgress(register);
                PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        // This callback will be invoked in two situations:
                        // 1 - Instant verification. In some cases the phone number can be instantly
                        //     verified without needing to send or enter a verification code.
                        // 2 - Auto-retrieval. On some devices Google Play services can automatically
                        //     detect the incoming verification SMS and perform verification without
                        //     user action.
                        smscode = credential.getSmsCode();
                        if (smscode != null) {
                            verifyCodes(smscode);
                        }
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        // This callback is invoked in an invalid request for verification is made,
                        // for instance if the the phone number format is not valid.
                        hideProgress(register);
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        // The SMS verification code has been sent to the provided phone number, we
                        // now need to ask the user to enter the code and then construct a credential
                        // by combining the code with a verification ID.

                        // Save verification ID and resending token so we can use them later

                        mVerificationId = verificationId;
                    }
                };
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+92" + phone,        // Phone number to verify
                        60,                 // Timeout duration
                        TimeUnit.SECONDS,   // Unit of timeout
                        requireActivity(),               // Activity (for callback binding)
                        mCallbacks);        // OnVerificationStateChangedCallbacks
            }
        }
    }

    private void PostRegistrationAnimation() {

        Animation fadeout = AnimationUtils.loadAnimation(getContext(), R.anim.fadeout);
        Animation fadein = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_fullscreen);
        fadein.setStartOffset(500);
        fadeout.setDuration(500);
        ConstraintLayout layout = registerFragment.findViewById(R.id.fullscreen_content);
        layout.startAnimation(fadeout);
        fadeout.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                layout.startAnimation(fadein);
                Email.setVisibility(View.GONE);
                Email.setEnabled(false);
                Email.setClickable(false);
                Password.setVisibility(View.GONE);
                Password.setEnabled(false);
                Password.setClickable(false);
                confirmPassword.setVisibility(View.GONE);
                confirmPassword.setEnabled(false);
                confirmPassword.setClickable(false);
                Phno.setVisibility(View.GONE);
                Phno.setEnabled(false);
                Phno.setClickable(false);
                Username.setVisibility(View.GONE);
                Username.setEnabled(false);
                Username.setClickable(false);
                register.setVisibility(View.GONE);
                register.setEnabled(false);
                register.setClickable(false);

                SMSCode.setVisibility(View.VISIBLE);
                verify.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void verifyCodes(String sms_code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, sms_code);
        Objects.requireNonNull(SMSCode.getEditText()).setText(sms_code);
        if (TextUtils.equals(SMSCode.getEditText().getText(), credential.getSmsCode())) {
            register(credential);
        } else {
            Toast.makeText(getContext(), "Invalid", Toast.LENGTH_SHORT).show();
        }
    }

    private void genKeys() {
        try {
            kpg = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        kpg.initialize(256);
        kp = kpg.genKeyPair();
        PublicKey publicKey = kp.getPublic();
        PrivateKey privateKey = kp.getPrivate();
        String encryptedText = "";
        String decryptedText = "";

        Cipher cipher;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(usrname.getBytes());
            encryptedText = Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(getContext(), encryptedText, Toast.LENGTH_LONG).show();

        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.decode(encryptedText, Base64.DEFAULT));
            decryptedText = new String(decryptedBytes);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        Toast.makeText(getContext(), decryptedText, Toast.LENGTH_LONG).show();

        PUK = Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
        PVK = Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT);
    }

    private void register(final PhoneAuthCredential credential) {
        if (Connectivity()) {
            showProgress(register);
            firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseFirestore.getInstance().collection("Users").whereEqualTo("Phone", 0 + phone).get().addOnCompleteListener(task12 -> {
                        if (task12.isSuccessful() && task12.getResult() != null) {
                            QuerySnapshot snapshots = task12.getResult();
                            boolean isEmpty = snapshots.getDocuments().isEmpty();
                            if (isEmpty) {
                                PostRegistrationAnimation();
                                firebaseAuth.createUserWithEmailAndPassword(email, pass)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                genKeys();

                                                final HashMap<String, Object> user = new HashMap<>();
                                                user.put("Name", usrname);
                                                user.put("Email", email);
                                                user.put("Phone", 0 + phone);
                                                user.put("PUK", PUK);
                                                user.put("PVK", PVK);
                                                user.put("FCM", "");
                                                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                                if (firebaseUser != null) {
                                                    firebaseUser.sendEmailVerification().addOnCompleteListener(task11 -> {
                                                        Toast.makeText(getContext(), "EMAIL SENT", Toast.LENGTH_LONG).show();
                                                        Toast.makeText(getContext(), "VERIFY EMAIL", Toast.LENGTH_LONG).show();
                                                        Toast.makeText(getContext(), "CHECK INBOX", Toast.LENGTH_LONG).show();
                                                        FirebaseFirestore.getInstance().collection("Users").document(0 + phone).set(user).addOnCompleteListener(task111 -> {
                                                            if (!task111.isSuccessful()) {
                                                                hideProgress(register);
                                                                if (task111.getException() != null) {
                                                                    Toast.makeText(getContext(), task111.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Toast.makeText(getContext(), "Couldn't send email", Toast.LENGTH_SHORT).show();
                                                                    Toast.makeText(getContext(), "Try again", Toast.LENGTH_LONG).show();
                                                                }
                                                            } else {
                                                                hideProgress(register);
                                                                firebaseAuth.signOut();
                                                                startActivity(new Intent(getActivity(), MainActivity.class));
                                                            }
                                                        });
                                                    });
                                                } else {
                                                    hideProgress(register);
                                                    Toast.makeText(getContext(), "Couldn't authenticate", Toast.LENGTH_SHORT).show();
                                                    Toast.makeText(getContext(), "Try again", Toast.LENGTH_LONG).show();
                                                }

                                            } else {
                                                firebaseAuth.signOut();
                                                hideProgress(register);
                                                if (task1.getException() != null) {
                                                    Toast.makeText(getContext(), "Error: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            } else {
                                firebaseAuth.signOut();
                                hideProgress(register);
                                Snackbar.make(requireActivity().getWindow().getDecorView().getRootView().findViewById((R.id.main_root)), "Phone No Already exists", Snackbar.LENGTH_INDEFINITE)
                                        .setAction("Sign In", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                startActivity(new Intent(getActivity(), MainActivity.class));
                                            }
                                        })
                                        .setActionTextColor(Color.parseColor("#FFDF5050"))
                                        .show();
                            }

                        }
                    });

                } else {
                    hideProgress(register);
                    if (task.getException() != null) {
                        Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Invalid", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
