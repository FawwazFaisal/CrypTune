package com.omnitech.cryptune.Fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.omnitech.cryptune.MainActivity;
import com.omnitech.cryptune.R;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class Login extends Fragment {
    private Button loginButton;
    private TextView forgot_pass;
    private TextInputLayout Email, Password;
    private View userLoginFragment;

    public Login() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        userLoginFragment = inflater.inflate(R.layout.fragment_login, container, false);
        userLoginFragment.setOnClickListener(v -> ((MainActivity) requireActivity()).hide());

        Email = userLoginFragment.findViewById(R.id.editText4);
        Password = userLoginFragment.findViewById((R.id.editText6));
        loginButton = userLoginFragment.findViewById(R.id.loginButton);

        forgot_pass = userLoginFragment.findViewById(R.id.forgotPass);
        loginButton.setOnClickListener(v -> new Handler().post(this::SignIn));
        forgot_pass.setOnClickListener(v -> forgotPassword());
        SpannableString content = new SpannableString("forgot password?");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        forgot_pass.setText(content);
        forgot_pass.setVisibility(View.GONE);

        setTextWatchers();
        return userLoginFragment;
    }

    private void setTextWatchers() {
        Email.getEditText().addTextChangedListener(new TextWatcher() {
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
        Password.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Password.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void SignIn() {
        ((MainActivity) requireActivity()).hide();
        final String email = Objects.requireNonNull(Email.getEditText()).getText().toString().trim();
        String pass = Objects.requireNonNull(Password.getEditText()).getText().toString().trim();
        boolean netState = ((MainActivity) requireActivity()).CheckConnectivity();

        if (TextUtils.isEmpty(email)) {
            Email.setError("Email Required");
            Email.requestFocus();
            Toast.makeText(getContext(), "Some fields are empty", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(pass)) {
            Password.setError("Password Required");
            Toast.makeText(getContext(), "Some fields are empty", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(pass) && TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), "Some fields are empty", Toast.LENGTH_SHORT).show();
        } else if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && netState) {
            ((MainActivity) requireActivity()).SignInUser(email, pass);
        }
    }

    public void enableLogin() {
        loginButton.setTextColor(Color.parseColor("#FFFFFF"));
        loginButton.setEnabled(true);
        loginButton.setClickable(true);
    }

    public void disableLogin() {
        loginButton.setTextColor(Color.rgb(145, 145, 145));
        loginButton.setEnabled(false);
        loginButton.setClickable(false);
    }

    public void show_forgot_pass() {
        forgot_pass.setVisibility(View.VISIBLE);
    }

    public void forgotPassword() {
        FirebaseAuth.getInstance().sendPasswordResetEmail(Email.getEditText().getText().toString().trim())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        forgot_pass.setTextColor(Color.parseColor("#88FFFFFF"));
                        forgot_pass.setEnabled(false);
                        forgot_pass.setClickable(false);
                        Snackbar.make(requireActivity().getWindow().getDecorView().getRootView().findViewById(R.id.main_root), "Password Reset Email Sent", Snackbar.LENGTH_INDEFINITE)
                                .setActionTextColor(Color.parseColor("#FFDF5050"))
                                .setAction("GOT IT", v -> {

                                })
                                .show();

                    } else if (!task.isSuccessful()) {
                        if (task.getException() != null) {
                            Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Try again", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
