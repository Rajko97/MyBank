package com.ana.mybank.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ana.mybank.Constants;
import com.ana.mybank.R;
import com.ana.mybank.ui.login.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText editName, editLastName, editEmail, editAccountNumber, editCardNumber, editCardPin;
    private TextInputLayout layoutName, layoutLastName, layoutEmail, layoutAccountNumber, layoutCardNumber, layoutCardPin;
    private MaterialButton submitButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeView();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeView() {
        setContentView(R.layout.activity_admin);
        setTitle("Admin panel");
        editName = findViewById(R.id.editTextName);
        editLastName = findViewById(R.id.editTextLastName);
        editEmail = findViewById(R.id.editTextEmail);
        editAccountNumber = findViewById(R.id.editTextAccountNumber);
        editCardNumber = findViewById(R.id.editTextCardNumber);
        editCardPin = findViewById(R.id.editTextCardPin);
        layoutName = findViewById(R.id.inputLayoutName);
        layoutLastName = findViewById(R.id.inputLayoutLastName);
        layoutEmail = findViewById(R.id.inputLayoutEmail);
        layoutAccountNumber = findViewById(R.id.inputLayoutAccountNumber);
        layoutCardNumber = findViewById(R.id.inputLayoutCardNumber);
        layoutCardPin = findViewById(R.id.inputLayoutCardPin);
        submitButton = findViewById(R.id.buttonSubmit);
        submitButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menuOptionLogout) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.buttonSubmit) {
            String name = editName.getText().toString();
            String lastName = editLastName.getText().toString();
            String email = editEmail.getText().toString();
            String accountNumber = editAccountNumber.getText().toString();
            String cardNumber = editCardNumber.getText().toString();
            String cardPin = editCardPin.getText().toString();

            boolean isNameValid = validateName(name);
            boolean isLastNameValid = validateLastName(lastName);
            boolean isEmailValid = validateEmail(email);
            boolean isAccountNumberValid = validateAccountNumber(accountNumber);
            boolean isCardNumberValid = validateCardNumber(cardNumber);
            boolean isCardPinValid = validateCardPin(cardPin);
            if(isNameValid && isLastNameValid && isEmailValid && isAccountNumberValid && isCardNumberValid && isCardPinValid) {
                submitButton.setEnabled(false);
                submitButton.setText("Waitting for network...");
                Map<String, Object> userData = new HashMap<>();
                    userData.put("name", name);
                    userData.put("lastName", lastName);
                    userData.put("accountNumber", accountNumber);
                    userData.put("cardNumber", cardNumber);
                    userData.put("securityCode", cardPin);
                    userData.put("balance", 0);

                createNewFirebaseUser(email, Constants.DEFAULT_PASSWORDS, userData);
            }
        }
    }

    //Creates Firebase user
    private void createNewFirebaseUser(String email, String password, final Map<String, Object> userData) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            initializeNewFirestoreUser(user.getUid(), userData);
                        } else {
                            submitButton.setEnabled(true);
                            submitButton.setText("SUBMIT");
                            Toast.makeText(AdminActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //creates Users/newDocument /CreditCards/newDocument
    private void initializeNewFirestoreUser(String uid, Map<String, Object> userData) {
        String securityCode = (String) userData.get("securityCode");
        String cardNumber = (String) userData.get("cardNumber");
        userData.remove("securityCode");

        db.collection("Users").document(uid).set(userData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        submitButton.setEnabled(true);
                        submitButton.setText("SUBMIT");
                        if(task.isSuccessful()) {
                            Toast.makeText(AdminActivity.this, "User successfully created", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminActivity.this, "Failed to create user", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        userData.clear();
        userData.put("securityCode", securityCode);
        db.collection("CreditCards").document(cardNumber).set(userData);
    }

    private boolean validateName(String name) {
        if(TextUtils.isEmpty(name)) {
            layoutName.setError("Enter name");
            return false;
        }
        layoutName.setError(null);
        return true;
    }
    private boolean validateLastName(String lastName) {
        if(TextUtils.isEmpty(lastName)) {
            layoutLastName.setError("Enter last name");
            return false;
        }
        layoutLastName.setError(null);
        return true;
    }

    private boolean validateEmail(String email) {
        if(TextUtils.isEmpty(email)) {
            layoutEmail.setError("Enter email");
            return false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Invalid format");
            return false;
        }
        layoutEmail.setError(null);
        return true;
    }

    private boolean validateAccountNumber(String accountNumber) {
        if(TextUtils.isEmpty(accountNumber)) {
            layoutAccountNumber.setError("Enter number");
            return false;
        }
        layoutAccountNumber.setError(null);
        return true;
    }

    private boolean validateCardNumber(String cardNumber) {
        if(TextUtils.isEmpty(cardNumber)) {
            layoutCardNumber.setError("Enter card number");
            return false;
        }
        layoutCardNumber.setError(null);
        return true;
    }

    private boolean validateCardPin(String cardPin) {
        if(TextUtils.isEmpty(cardPin)) {
            layoutCardPin.setError("Enter card pin");
            return false;
        }
        layoutCardPin.setError(null);
        return true;
    }
}
