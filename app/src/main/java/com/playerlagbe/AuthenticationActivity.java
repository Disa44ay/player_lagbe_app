package com.playerlagbe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AuthenticationActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private RadioGroup roleGroup;
    private Button loginButton, googleSignInButton;
    private TextView forgotPasswordText, signupRedirectText;
    private SharedPreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        prefsManager = new SharedPreferencesManager(this);

        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        roleGroup = findViewById(R.id.roleGroup);
        loginButton = findViewById(R.id.loginButton);
        googleSignInButton = findViewById(R.id.googleSignIn);
        forgotPasswordText = findViewById(R.id.forgotPassword);
        signupRedirectText = findViewById(R.id.signupRedirect);

        loginButton.setOnClickListener(v -> {
            String usernameOrEmail = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (usernameOrEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Find username by email or treat input as username
            String username = usernameOrEmail;
            // If input looks like email, try to find corresponding username
            if (Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches()) {
                // Scan all users for matching email
                username = findUsernameByEmail(usernameOrEmail);
                if (username == null) {
                    Toast.makeText(this, "Email not registered", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Validate user credentials
            if (!prefsManager.validateUser(username, password)) {
                Toast.makeText(this, "Invalid username/email or password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Set login state and current user
            prefsManager.setLoggedIn(true);
            prefsManager.setCurrentUser(username);

            // Get role from RadioGroup (optional usage)
            int selectedRoleId = roleGroup.getCheckedRadioButtonId();
            String role = "User"; // default
            if (selectedRoleId == R.id.radioAdmin) {
                role = "Admin";
            }

            Toast.makeText(this, "Logging in as " + role, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        googleSignInButton.setOnClickListener(v -> {
            Toast.makeText(this, "Google Sign-In clicked (not implemented)", Toast.LENGTH_SHORT).show();
        });

        forgotPasswordText.setOnClickListener(v -> {
            startActivity(new Intent(AuthenticationActivity.this, ForgotPasswordActivity.class));
        });

        signupRedirectText.setOnClickListener(v -> {
            startActivity(new Intent(AuthenticationActivity.this, SignupActivity.class));
        });
    }

    // Helper method to find username by email stored in prefs
    private String findUsernameByEmail(String email) {
        // Unfortunately SharedPreferencesManager does not have a method to get all usernames,
        // so you need to scan keys or maintain a user list separately.
        // For simplicity, assume username = email prefix if stored, or just return null here.
        // Better approach: store a mapping of emails to usernames during signup.
        // Here, I return null as a placeholder.
        return null;
    }
}
