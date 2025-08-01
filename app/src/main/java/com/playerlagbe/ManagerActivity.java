package com.playerlagbe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ManagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        // Set up hamburger menu
        setupHamburgerMenu();
        
        // Set up bottom navigation
        setupBottomNavigation();
    }

    private void setupHamburgerMenu() {
        ImageView hamburgerMenuIcon = findViewById(R.id.hamburgerMenuIcon);
        if (hamburgerMenuIcon != null) {
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
                        startActivity(new Intent(ManagerActivity.this, ProfileActivity.class));
                        return true;
                    } else if (id == R.id.menu_cart) {
                        startActivity(new Intent(ManagerActivity.this, CartActivity.class));
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
                        FirebaseAuthManager authManager = new FirebaseAuthManager(this);
                        authManager.signOut();
                        Intent intent = new Intent(ManagerActivity.this, AuthenticationActivity.class);
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
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        if (bottomNavigationView != null) {
            // Set the manager item as selected
            bottomNavigationView.setSelectedItemId(R.id.nav_manager);
            
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(ManagerActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_teams) {
                    Intent intent = new Intent(ManagerActivity.this, TeamsActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_shop) {
                    Intent intent = new Intent(ManagerActivity.this, ShopActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_manager) {
                    // Already on manager, do nothing
                    return true;
                }
                return false;
            });
        }
    }
}