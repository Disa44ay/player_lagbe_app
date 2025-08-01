package com.playerlagbe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {

    ImageView profileIcon, cartIcon, themeToggleIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if user is logged in
        FirebaseAuthManager authManager = new FirebaseAuthManager(this);
        if (!authManager.isUserSignedIn()) {
            Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Start with HomeActivity by default
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
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
