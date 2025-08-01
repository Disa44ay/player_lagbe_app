package com.playerlagbe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;

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

        fragmentManager = getSupportFragmentManager();
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        
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
                loadFragment(selectedFragment, false);
                return true;
            }
            return false;
        });

        // Load default fragment (Home) if this is the first time
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    public void handleHamburgerMenuAction(int actionId) {
        if (actionId == R.id.menu_profile) {
            loadFragment(new ProfileFragment(), true);
        } else if (actionId == R.id.menu_cart) {
            loadFragment(new CartFragment(), true);
        } else if (actionId == R.id.menu_theme) {
            toggleTheme();
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
