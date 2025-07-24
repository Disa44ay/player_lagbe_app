package com.playerlagbe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

/**
 * Signup Activity using Firebase Authentication
 * Handles user registration with email and password
 */
public class SignupActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirmPassword, etEmail;
    private Button btnSignup;
    private ImageButton btnBackToAuth;
    private ProgressBar progressBar;
    private FirebaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth Manager
        authManager = new FirebaseAuthManager(this);

        // Initialize views
        initializeViews();

        // Set up click listeners
        setupClickListeners();
    }

    /**
     * Initialize all views
     */
    private void initializeViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etEmail = findViewById(R.id.et_email);
        btnSignup = findViewById(R.id.btn_signup);
        btnBackToAuth = findViewById(R.id.btn_back_to_auth);
        
        // Add progress bar (you may need to add this to your layout)
        progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Set up click listeners
     */
    private void setupClickListeners() {
        btnSignup.setOnClickListener(v -> performSignup());
        btnBackToAuth.setOnClickListener(v -> finish());
    }

    /**
     * Perform user registration
     */
    private void performSignup() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // Validation
        if (!validateInput(username, password, confirmPassword, email)) {
            return;
        }

        // Disable signup button during registration
        setSignupButtonEnabled(false);

        // Register with Firebase including username
        authManager.registerWithEmailPasswordAndUsername(email, password, username, authListener);
    }

    /**
     * Validate user input
     */
    private boolean validateInput(String username, String password, String confirmPassword, String email) {
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()) {
            showError("Please fill all fields");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Invalid email format");
            return false;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return false;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters");
            return false;
        }

        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            showError("Username can only contain letters, numbers, and underscores");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return false;
        }

        return true;
    }

    /**
     * Firebase Authentication listener
     */
    private final FirebaseAuthManager.AuthListener authListener = new FirebaseAuthManager.AuthListener() {
        @Override
        public void onAuthSuccess(FirebaseUser user) {
            runOnUiThread(() -> {
                setSignupButtonEnabled(true);
                
                if (user != null) {
                    String username = etUsername.getText().toString().trim();
                    showSuccessDialog("Account Created!", 
                            "Welcome to Player Lagbe, " + username + "! Your account has been created successfully.");
                }
            });
        }

        @Override
        public void onAuthFailure(String error) {
            runOnUiThread(() -> {
                setSignupButtonEnabled(true);
                showError(error);
            });
        }

        @Override
        public void onAuthLoading(boolean isLoading) {
            runOnUiThread(() -> {
                if (progressBar != null) {
                    progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                }
                setSignupButtonEnabled(!isLoading);
            });
        }
    };

    /**
     * Enable/disable signup button
     */
    private void setSignupButtonEnabled(boolean enabled) {
        btnSignup.setEnabled(enabled);
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Show success message with dialog
     */
    private void showSuccessDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Continue", (dialog, which) -> {
                    // Navigate to MainActivity after user clicks Continue
                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        
        // Check if user is already signed in
        if (authManager.isUserSignedIn()) {
            FirebaseUser currentUser = authManager.getCurrentUser();
            if (currentUser != null) {
                // User is already signed in, go to MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }
    }
}
