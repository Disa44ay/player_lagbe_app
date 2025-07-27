package com.playerlagbe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.playerlagbe.fragments.HomeFragment;
import com.playerlagbe.fragments.ManagerFragment;
import com.playerlagbe.fragments.ShopFragment;
import com.playerlagbe.fragments.TeamsFragment;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.SharedPreferences;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ImageView profileIcon, cartIcon, themeToggleIcon;
    com.google.android.material.floatingactionbutton.FloatingActionButton fabProfile, fabCart, fabTheme, fabLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fabProfile = findViewById(R.id.fabProfile);
        fabCart = findViewById(R.id.fabCart);
        fabTheme = findViewById(R.id.fabTheme);
        fabLogout = findViewById(R.id.fabLogout);

        fabProfile.setOnClickListener(v -> openProfile());
        fabCart.setOnClickListener(v -> openCart());
        fabTheme.setOnClickListener(v -> toggleTheme());
        fabLogout.setOnClickListener(v -> {
            FirebaseAuthManager authManager = new FirebaseAuthManager(this);
            authManager.signOut();
            Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Check if user is logged in
        FirebaseAuthManager authManager = new FirebaseAuthManager(this);
        if (!authManager.isUserSignedIn()) {
            Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
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

    private void openProfile() {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void openCart() {
        Intent intent = new Intent(MainActivity.this, CartActivity.class);
        startActivity(intent);
    }

    private void toggleTheme() {
        boolean isDarkMode = isDarkMode();
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            saveThemePref(false);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            saveThemePref(true);
        }
        updateThemeToggleIcon();
    }

    private void updateThemeToggleIcon() {
        boolean isDarkMode = isDarkMode();
        if (themeToggleIcon != null) {
            themeToggleIcon.setImageResource(isDarkMode ? R.drawable.ic_theme_toggle : R.drawable.ic_theme_toggle);
            // Optionally, you can use different icons for light/dark
        }
    }

    private boolean isDarkMode() {
        return getSharedPreferences("settings", MODE_PRIVATE).getBoolean("dark_mode", false);
    }

    private void saveThemePref(boolean darkMode) {
        getSharedPreferences("settings", MODE_PRIVATE).edit().putBoolean("dark_mode", darkMode).apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateThemeToggleIcon();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Apply theme based on saved preference
        boolean darkMode = isDarkMode();
        AppCompatDelegate.setDefaultNightMode(darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
