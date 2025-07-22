package com.playerlagbe;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etUsername, etNewPassword, etConfirmNewPassword;
    private Button btnResetPassword, btnBackToAuth;
    private SharedPreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        prefsManager = new SharedPreferencesManager(this);

        etUsername = findViewById(R.id.et_username);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmNewPassword = findViewById(R.id.et_confirm_new_password);
        btnResetPassword = findViewById(R.id.btn_reset_password);
        btnBackToAuth = findViewById(R.id.btn_back_to_auth);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String newPassword = etNewPassword.getText().toString().trim();
                String confirmNewPassword = etConfirmNewPassword.getText().toString().trim();

                if (username.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmNewPassword)) {
                    Toast.makeText(ForgotPasswordActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!prefsManager.userExists(username)) {
                    Toast.makeText(ForgotPasswordActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update password
                prefsManager.updatePassword(username, newPassword);
                Toast.makeText(ForgotPasswordActivity.this, "Password reset successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnBackToAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}