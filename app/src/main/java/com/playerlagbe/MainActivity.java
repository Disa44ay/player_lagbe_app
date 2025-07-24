package com.playerlagbe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Main Activity - Home screen for authenticated users
 * Shows user information and provides logout functionality
 */
public class MainActivity extends AppCompatActivity {

    private Button btnLogout;
    private TextView tvWelcome, tvUserEmail;
    private FirebaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth Manager
        authManager = new FirebaseAuthManager(this);

        // Initialize views
        initializeViews();

        // Display user information
        displayUserInfo();

        // Set up logout functionality
        setupLogout();
    }

    /**
     * Initialize all views
     */
    private void initializeViews() {
        btnLogout = findViewById(R.id.btnLogout);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserEmail = findViewById(R.id.tvUserEmail);
    }

    /**
     * Display current user information
     */
    private void displayUserInfo() {
        FirebaseUser currentUser = authManager.getCurrentUser();
        
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            
            if (tvWelcome != null) {
                String welcomeText = "Welcome" + (displayName != null ? ", " + displayName : "");
                tvWelcome.setText(welcomeText);
            }
            
            if (tvUserEmail != null && email != null) {
                tvUserEmail.setText(email);
            }
        } else {
            // User is not signed in, redirect to authentication
            redirectToAuth();
        }
    }

    /**
     * Set up logout functionality
     */
    private void setupLogout() {
        btnLogout.setOnClickListener(v -> {
            // Sign out from Firebase
            authManager.signOut();
            
            // Redirect to authentication screen
            redirectToAuth();
        });
    }

    /**
     * Redirect to authentication activity
     */
    private void redirectToAuth() {
        Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        
        // Check if user is still signed in
        if (!authManager.isUserSignedIn()) {
            redirectToAuth();
        }
    }
}
