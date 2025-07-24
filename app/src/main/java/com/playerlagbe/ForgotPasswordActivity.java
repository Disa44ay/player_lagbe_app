package com.playerlagbe;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

/**
 * Forgot Password Activity using Firebase Authentication
 * Sends password reset email to the user
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnResetPassword;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private FirebaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

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
        btnBack = findViewById(R.id.btnBack);
        etEmail = findViewById(R.id.et_email); // Changed from username to email
        btnResetPassword = findViewById(R.id.btn_reset_password);
        
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
        btnBack.setOnClickListener(v -> finish());
        btnResetPassword.setOnClickListener(v -> performPasswordReset());
    }

    /**
     * Perform password reset
     */
    private void performPasswordReset() {
        String email = etEmail.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            showError("Please enter your email address");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address");
            return;
        }

        // Disable reset button during operation
        setResetButtonEnabled(false);

        // Send password reset email
        authManager.sendPasswordResetEmail(email, authListener);
    }

    /**
     * Firebase Authentication listener
     */
    private final FirebaseAuthManager.AuthListener authListener = new FirebaseAuthManager.AuthListener() {
        @Override
        public void onAuthSuccess(FirebaseUser user) {
            runOnUiThread(() -> {
                setResetButtonEnabled(true);
                Toast.makeText(ForgotPasswordActivity.this, 
                        "Password reset email sent! Check your inbox.", 
                        Toast.LENGTH_LONG).show();
                finish();
            });
        }

        @Override
        public void onAuthFailure(String error) {
            runOnUiThread(() -> {
                setResetButtonEnabled(true);
                showError(error);
            });
        }

        @Override
        public void onAuthLoading(boolean isLoading) {
            runOnUiThread(() -> {
                if (progressBar != null) {
                    progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                }
                setResetButtonEnabled(!isLoading);
            });
        }
    };

    /**
     * Enable/disable reset button
     */
    private void setResetButtonEnabled(boolean enabled) {
        btnResetPassword.setEnabled(enabled);
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
