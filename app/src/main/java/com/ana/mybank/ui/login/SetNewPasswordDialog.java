package com.ana.mybank.ui.login;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;

import com.ana.mybank.Constants;
import com.ana.mybank.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SetNewPasswordDialog extends Dialog implements View.OnClickListener {
    private Context context;
    private TextInputLayout textInputLayout;
    private TextInputEditText textInputEditText;
    private MaterialButton button;

    public SetNewPasswordDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_enter_new_password);
        textInputLayout = findViewById(R.id.inputLayoutNewPassword);
        textInputEditText = findViewById(R.id.editTextNewPassword);
        button = findViewById(R.id.buttonSetPassword);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.buttonSetPassword) {
            String newPassword = textInputEditText.getText().toString();
            if(TextUtils.isEmpty(newPassword)) {
                textInputLayout.setError("Field cant be empty");
                return;
            }
            if(newPassword.equals(Constants.DEFAULT_PASSWORDS)) {
                textInputLayout.setError("You can't set the same password");
                return;
            }
            textInputLayout.setError(null);
            button.setEnabled(false);
            button.setText("Waiting for network...");
            onNewPasswordSet(newPassword);
        }
    }

    public void onNewPasswordSet(String newPassword) {

    }
}
