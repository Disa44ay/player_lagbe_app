package com.playerlagbe.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.playerlagbe.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.Button;
import com.playerlagbe.FirebaseAuthManager;
import com.playerlagbe.AuthenticationActivity;
import com.playerlagbe.ProfileActivity;
import com.playerlagbe.CartActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.view.MenuInflater;
import android.widget.PopupMenu;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Hamburger menu logic using PopupMenu
        ImageView hamburgerMenuIcon = view.findViewById(R.id.hamburgerMenuIcon);
        hamburgerMenuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), hamburgerMenuIcon);
            popup.getMenuInflater().inflate(R.menu.menu_hamburger, popup.getMenu());
            // Set icons for menu items
            popup.getMenu().findItem(R.id.menu_profile).setIcon(R.drawable.ic_profile);
            popup.getMenu().findItem(R.id.menu_cart).setIcon(R.drawable.ic_cart);
            popup.getMenu().findItem(R.id.menu_theme).setIcon(R.drawable.ic_theme_toggle);
            popup.getMenu().findItem(R.id.menu_logout).setIcon(R.drawable.ic_logout);
            // Set theme label
            SharedPreferences prefs = requireContext().getSharedPreferences("settings", 0);
            boolean isDarkMode = prefs.getBoolean("dark_mode", false);
            popup.getMenu().findItem(R.id.menu_theme).setTitle(isDarkMode ? "Light Mode" : "Dark Mode");
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_profile) {
                    startActivity(new Intent(getActivity(), com.playerlagbe.ProfileActivity.class));
                    return true;
                } else if (id == R.id.menu_cart) {
                    startActivity(new Intent(getActivity(), com.playerlagbe.CartActivity.class));
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
                    requireActivity().recreate();
                    return true;
                } else if (id == R.id.menu_logout) {
                    com.playerlagbe.FirebaseAuthManager authManager = new com.playerlagbe.FirebaseAuthManager(requireContext());
                    authManager.signOut();
                    Intent intent = new Intent(getActivity(), com.playerlagbe.AuthenticationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                    Toast.makeText(getActivity(), "Logged out", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
            popup.show();
        });
        return view;
    }
}
