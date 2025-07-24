package com.playerlagbe;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Launcher Activity that checks Firebase Authentication state
 * and redirects users to the appropriate screen
 */
public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check Firebase authentication state
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // User is signed in, redirect to MainActivity
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // User is not signed in, redirect to AuthenticationActivity
            startActivity(new Intent(this, AuthenticationActivity.class));
        }

        // Close this launcher activity
        finish();
    }
}
