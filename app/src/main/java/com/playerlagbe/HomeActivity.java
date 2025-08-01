package com.playerlagbe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Set up cart icon click listener
        View topBarContainer = findViewById(R.id.topAppBar);
        if (topBarContainer != null) {
            ImageView cartIcon = topBarContainer.findViewById(R.id.cartIcon);
            if (cartIcon != null) {
                cartIcon.setOnClickListener(v -> {
                    Intent intent = new Intent(HomeActivity.this, CartActivity.class);
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
            // Set the home item as selected
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    // Already on home, do nothing
                    return true;
                } else if (itemId == R.id.nav_teams) {
                    Intent intent = new Intent(HomeActivity.this, TeamsActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_shop) {
                    Intent intent = new Intent(HomeActivity.this, ShopActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_manager) {
                    Intent intent = new Intent(HomeActivity.this, ManagerActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            });
        }
    }
}
