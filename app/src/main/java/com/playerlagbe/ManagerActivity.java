package com.playerlagbe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ManagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        // Set up cart icon click listener
        View topBarContainer = findViewById(R.id.topAppBar);
        if (topBarContainer != null) {
            ImageView cartIcon = topBarContainer.findViewById(R.id.cartIcon);
            if (cartIcon != null) {
                cartIcon.setOnClickListener(v -> {
                    Intent intent = new Intent(ManagerActivity.this, CartActivity.class);
                    startActivity(intent);
                });
            }
        }

        // Set up bottom navigation
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
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