package com.playerlagbe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.playerlagbe.fragments.HomeFragment;
import com.playerlagbe.fragments.ManagerFragment;
import com.playerlagbe.fragments.ShopFragment;
import com.playerlagbe.fragments.TeamsFragment;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Toolbar toolbar;
    Button logoutButton;
    FirebaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth Manager
        authManager = new FirebaseAuthManager(this);

        // Check if user is signed in
        if (!authManager.isUserSignedIn()) {
            redirectToLogin();
            return;
        }

        // Initialize views
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        toolbar = findViewById(R.id.toolbar);
        logoutButton = findViewById(R.id.logoutButton);

        // Set up toolbar
        setSupportActionBar(toolbar);

        // Set up logout button
        logoutButton.setOnClickListener(v -> performLogout());

        // Set up bottom navigation
        openFragment(new HomeFragment()); // Default fragment

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_teams) {
                selectedFragment = new TeamsFragment();
            } else if (itemId == R.id.nav_shop) {
                selectedFragment = new ShopFragment();
            } else if (itemId == R.id.nav_manager) {
                selectedFragment = new ManagerFragment();
            }

            if (selectedFragment != null) {
                openFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void performLogout() {
        try {
            authManager.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        } catch (Exception e) {
            Toast.makeText(this, "Error logging out", Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check authentication status when activity starts
        if (!authManager.isUserSignedIn()) {
            redirectToLogin();
        }
    }
}
