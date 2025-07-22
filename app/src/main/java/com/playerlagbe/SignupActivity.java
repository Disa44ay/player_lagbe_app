package com.playerlagbe;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirmPassword, etEmail;
    private Button btnSignup, btnBackToAuth;
    private SharedPreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        prefsManager = new SharedPreferencesManager(this);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etEmail = findViewById(R.id.et_email);
        btnSignup = findViewById(R.id.btn_signup);
        btnBackToAuth = findViewById(R.id.btn_back_to_auth);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();
                String email = etEmail.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (prefsManager.userExists(username)) {
                    Toast.makeText(SignupActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save user credentials
                prefsManager.saveUser(username, password, email);
                Toast.makeText(SignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
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