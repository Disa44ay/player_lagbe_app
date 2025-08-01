package com.playerlagbe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.SharedPreferences;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.Button;
import android.view.MenuInflater;
import android.widget.PopupMenu;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.playerlagbe.fragments.HomeContentFragment;
import com.playerlagbe.fragments.ManagerFragment;
import com.playerlagbe.fragments.ShopFragment;
import com.playerlagbe.fragments.TeamsFragment;

public class ShopActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ImageView profileIcon, cartIcon, themeToggleIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_shared);

        // Check if user is logged in
        FirebaseAuthManager authManager = new FirebaseAuthManager(this);
        if (!authManager.isUserSignedIn()) {
            Intent intent = new Intent(ShopActivity.this, AuthenticationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        openFragment(new ShopFragment()); // Default fragment

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeContentFragment();
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

        // Hamburger menu logic using PopupMenu
        ImageView hamburgerMenuIcon = findViewById(R.id.hamburgerMenuIcon);
        hamburgerMenuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, hamburgerMenuIcon);
            popup.getMenuInflater().inflate(R.menu.menu_hamburger, popup.getMenu());
            // Set icons for menu items
            popup.getMenu().findItem(R.id.menu_profile).setIcon(R.drawable.ic_profile);
            popup.getMenu().findItem(R.id.menu_cart).setIcon(R.drawable.ic_cart);
            popup.getMenu().findItem(R.id.menu_theme).setIcon(R.drawable.ic_theme_toggle);
            popup.getMenu().findItem(R.id.menu_logout).setIcon(R.drawable.ic_logout);
            // Set theme label
            SharedPreferences prefs = getSharedPreferences("settings", 0);
            boolean isDarkMode = prefs.getBoolean("dark_mode", false);
            popup.getMenu().findItem(R.id.menu_theme).setTitle(isDarkMode ? "Light Mode" : "Dark Mode");
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    return true;
                } else if (id == R.id.menu_cart) {
                    startActivity(new Intent(this, CartActivity.class));
                    return true;
                } else if (id == R.id.menu_theme) {
                    boolean dark = prefs.getBoolean("dark_mode", false);
                    if (dark) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        prefs.edit().putBoolean("dark_mode", false).apply();
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        prefs.edit().putBoolean("dark_mode", true).apply();
                    }
                    recreate();
                    return true;
                } else if (id == R.id.menu_logout) {
                    FirebaseAuthManager authManager2 = new FirebaseAuthManager(this);
                    authManager2.signOut();
                    Intent intent = new Intent(this, AuthenticationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void openProfile() {
        Intent intent = new Intent(ShopActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void openCart() {
        Intent intent = new Intent(ShopActivity.this, CartActivity.class);
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