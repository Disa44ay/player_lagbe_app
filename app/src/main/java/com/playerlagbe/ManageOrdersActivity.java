package com.playerlagbe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class ManageOrdersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        // Check admin status
        checkAdminAccess();

        // Setup hamburger menu
        setupHamburgerMenu();
    }

    private void checkAdminAccess() {
        FirebaseAuthManager authManager = new FirebaseAuthManager(this);
        authManager.checkAdminStatus(new FirebaseAuthManager.AdminCheckListener() {
            @Override
            public void onAdminCheckResult(boolean isAdmin) {
                if (!isAdmin) {
                    Toast.makeText(ManageOrdersActivity.this, "Access denied. Admin only.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onAdminCheckError(String error) {
                Toast.makeText(ManageOrdersActivity.this, "Could not verify admin status. Access denied.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupHamburgerMenu() {
        ImageView hamburgerMenuIcon = findViewById(R.id.hamburgerMenuIcon);
        if (hamburgerMenuIcon != null) {
            hamburgerMenuIcon.setOnClickListener(this::showHamburgerMenu);
        }
    }

    private void showHamburgerMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.menu_hamburger, popup.getMenu());
        
        // Show admin menu items
        popup.getMenu().findItem(R.id.menu_manage_team).setVisible(true);
        popup.getMenu().findItem(R.id.menu_manage_shop).setVisible(true);
        popup.getMenu().findItem(R.id.menu_manage_orders).setVisible(true);
        
        // Set theme toggle text
        SharedPreferences prefs = getSharedPreferences("settings", 0);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        popup.getMenu().findItem(R.id.menu_theme).setTitle(isDarkMode ? "Light Mode" : "Dark Mode");
        
        popup.setOnMenuItemClickListener(item -> {
            handleHamburgerMenuAction(item.getItemId());
            return true;
        });
        popup.show();
    }

    private void handleHamburgerMenuAction(int actionId) {
        if (actionId == R.id.menu_profile) {
            // Navigate back to main activity and show profile
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("fragment", "profile");
            startActivity(intent);
        } else if (actionId == R.id.menu_cart) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("fragment", "cart");
            startActivity(intent);
        } else if (actionId == R.id.menu_theme) {
            toggleTheme();
        } else if (actionId == R.id.menu_manage_team) {
            startActivity(new Intent(this, ManageTeamActivity.class));
        } else if (actionId == R.id.menu_manage_shop) {
            startActivity(new Intent(this, ManageShopActivity.class));
        } else if (actionId == R.id.menu_manage_orders) {
            // Already in manage orders
        } else if (actionId == R.id.menu_logout) {
            logout();
        }
    }

    private void toggleTheme() {
        SharedPreferences prefs = getSharedPreferences("settings", 0);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            prefs.edit().putBoolean("dark_mode", false).apply();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            prefs.edit().putBoolean("dark_mode", true).apply();
        }
        recreate();
    }

    private void logout() {
        FirebaseAuthManager authManager = new FirebaseAuthManager(this);
        authManager.signOut();
        Intent intent = new Intent(this, AuthenticationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}