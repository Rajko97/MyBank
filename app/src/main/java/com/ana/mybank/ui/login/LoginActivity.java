package com.ana.mybank.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.ana.mybank.Constants;
import com.ana.mybank.R;
import com.ana.mybank.ui.admin.AdminActivity;
import com.ana.mybank.ui.main.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private TextInputLayout layoutEmail, layoutPassword;
    private TextInputEditText editEmail, editPassword;
    private MaterialButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkIfIsUserLoggedIn();
        initializeView();
    }

    private void checkIfIsUserLoggedIn() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            startNextActivity(mAuth.getCurrentUser());
        }
    }

    private void initializeView() {
        setContentView(R.layout.activity_login);
        layoutEmail = findViewById(R.id.inputLayoutEmail);
        layoutPassword = findViewById(R.id.inputLayoutPassword);
        editEmail = findViewById(R.id.editTextEmail);
        editPassword = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.loginButton) {
            doValidation();
        }
    }

    @SuppressLint("SetTextI18n")
    private void doValidation() {
        String email = Objects.requireNonNull(editEmail.getText()).toString();
        String password = Objects.requireNonNull(editPassword.getText()).toString();

        boolean isEmailValid = validateEmail(email);
        boolean isPasswordValid = validatePassword(password);

        if (isEmailValid && isPasswordValid) {
            loginButton.setEnabled(false);
            loginButton.setText("Wait for network...");
            signInExistingUser(email, password);
        } else {
            Toast.makeText(LoginActivity.this, "Check all fields", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateEmail(String email) {
        if(TextUtils.isEmpty(email)) {
            layoutEmail.setError("Email field can't be empty");
            return false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Invalid email format");
            return false;
        }
        layoutEmail.setError(null);
        return true;
    }
    private boolean validatePassword(String password) {
        if(TextUtils.isEmpty(password)) {
            layoutPassword.setError("Password field can't be empty");
            return false;
        }
        layoutPassword.setError(null);
        return true;
    }

    private void signInExistingUser(final String email, final String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if(password.equals(Constants.DEFAULT_PASSWORDS))
                            {
                                setNewPassword(email);
                            } else {
                                startNextActivity(Objects.requireNonNull(mAuth.getCurrentUser()));
                            }
                        } else {
                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                layoutPassword.setError("Wrong password");
                            } else if(task.getException() instanceof FirebaseAuthInvalidUserException) {
                                layoutEmail.setError("Wrong email address");
                            } else {
                                Toast.makeText(LoginActivity.this, "Unknown error occurred", Toast.LENGTH_SHORT).show();
                            }
                            loginButton.setEnabled(true);
                            loginButton.setText("Login");
                        }
                    }

                    private void setNewPassword(String email) {
                        showInputNewPasswordDialog(email);
                    }

                    private void showInputNewPasswordDialog(final String email) {
                        SetNewPasswordDialog dialog = new SetNewPasswordDialog(LoginActivity.this) {
                            @Override
                            public void onNewPasswordSet(final String newPassword) {
                                final FirebaseUser user = mAuth.getCurrentUser();

                                AuthCredential credential = EmailAuthProvider.getCredential(email, Constants.DEFAULT_PASSWORDS);
                                if(user != null)
                                user.reauthenticate(credential)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                startNextActivity(user);
                                                            } else {
                                                                Toast.makeText(getApplicationContext(), "Failed to set new password", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Failed to set new password", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        };
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                });
    }

    private void startNextActivity(@NonNull FirebaseUser user) {
        if(isUserAdmin(user)) {
            startActivity(new Intent(LoginActivity.this, AdminActivity.class));
        } else {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }
        finish();
    }
    private boolean isUserAdmin(@NonNull FirebaseUser user) {
        for (String adminEmail: Constants.ADMIN_MAILS) {
            if(adminEmail.equals(user.getEmail()))
                return true;
        }
        return false;
    }
}
