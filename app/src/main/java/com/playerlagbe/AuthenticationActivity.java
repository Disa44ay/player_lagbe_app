package com.playerlagbe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

/**
 * Authentication Activity using Firebase Auth
 * 
 * This activity provides:
 * - Email/Password login with comprehensive validation
 * - Username-based login (automatically resolves to email)
 * - Google Sign-In integration with proper error handling
 * - Role selection (User/Admin)
 * - Automatic user session management
 * - Responsive UI with loading indicators
 * 
 * The activity automatically redirects authenticated users to MainActivity
 * and provides seamless navigation to signup and password reset screens.
 */
public class AuthenticationActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton, googleSignInButton;
    private TextView forgotPasswordText, signupRedirectText;
    private ProgressBar progressBar;
    private FirebaseAuthManager authManager;

    // Activity result launcher for Google Sign-In
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        // Initialize Firebase Auth Manager
        authManager = new FirebaseAuthManager(this);

        // Initialize views
        initializeViews();

        // Set up Google Sign-In launcher
        setupGoogleSignInLauncher();

        // Set up click listeners
        setupClickListeners();
    }

    /**
     * Initialize all views
     */
    private void initializeViews() {
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        googleSignInButton = findViewById(R.id.googleSignIn);
        forgotPasswordText = findViewById(R.id.forgotPassword);
        signupRedirectText = findViewById(R.id.signupRedirect);
        
        // Add progress bar (you may need to add this to your layout)
        progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Set up Google Sign-In activity result launcher
     */
    private void setupGoogleSignInLauncher() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        authManager.handleGoogleSignInResult(data, authListener);
                    } else {
                        showError("Google Sign-In cancelled");
                    }
                }
        );
    }

    /**
     * Set up click listeners for all buttons
     */
    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> performEmailPasswordLogin());
        
        googleSignInButton.setOnClickListener(v -> performGoogleSignIn());
        
        forgotPasswordText.setOnClickListener(v -> 
                startActivity(new Intent(AuthenticationActivity.this, ForgotPasswordActivity.class))
        );
        
        signupRedirectText.setOnClickListener(v -> 
                startActivity(new Intent(AuthenticationActivity.this, SignupActivity.class))
        );
    }

    /**
     * Perform email/username/password login
     */
    private void performEmailPasswordLogin() {
        String emailOrUsername = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Basic validation
        if (emailOrUsername.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        // For email login, validate email format
        if (emailOrUsername.contains("@") && !Patterns.EMAIL_ADDRESS.matcher(emailOrUsername).matches()) {
            showError("Please enter a valid email address");
            return;
        }
        
        // For username login, validate username format
        if (!emailOrUsername.contains("@") && emailOrUsername.length() < 3) {
            showError("Username must be at least 3 characters");
            return;
        }

        // Disable login button during authentication
        setLoginButtonEnabled(false);

        // Provide feedback based on input type
        if (emailOrUsername.contains("@")) {
            Toast.makeText(this, "Signing in with email...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Looking up username and signing in...", Toast.LENGTH_SHORT).show();
        }

        // Sign in with Firebase (supports both email and username)
        authManager.signIn(emailOrUsername, password, authListener);
    }

    /**
     * Perform Google Sign-In
     */
    private void performGoogleSignIn() {
        setLoginButtonEnabled(false);
        Intent signInIntent = authManager.getGoogleSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    /**
     * Firebase Authentication listener
     */
    private final FirebaseAuthManager.AuthListener authListener = new FirebaseAuthManager.AuthListener() {
        @Override
        public void onAuthSuccess(FirebaseUser user) {
            runOnUiThread(() -> {
                setLoginButtonEnabled(true);
                
                if (user != null) {
                    String welcomeName = user.getDisplayName() != null ? user.getDisplayName() : 
                                        (user.getEmail() != null ? user.getEmail().split("@")[0] : "User");
                    Toast.makeText(AuthenticationActivity.this, 
                            "Welcome back, " + welcomeName + "!", 
                            Toast.LENGTH_SHORT).show();
                    
                    // Navigate to MainActivity
                    Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // This case is for password reset success
                    Toast.makeText(AuthenticationActivity.this, 
                            "Password reset email sent", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onAuthFailure(String error) {
            runOnUiThread(() -> {
                setLoginButtonEnabled(true);
                showError(error);
            });
        }

        @Override
        public void onAuthLoading(boolean isLoading) {
            runOnUiThread(() -> {
                if (progressBar != null) {
                    progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                }
                setLoginButtonEnabled(!isLoading);
            });
        }
    };



    /**
     * Enable/disable login buttons
     */
    private void setLoginButtonEnabled(boolean enabled) {
        loginButton.setEnabled(enabled);
        googleSignInButton.setEnabled(enabled);
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
                .setPositiveButton("OK", null)
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
