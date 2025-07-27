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

        // Hamburger menu logic
        ImageView hamburgerMenuIcon = view.findViewById(R.id.hamburgerMenuIcon);
        LinearLayout hamburgerMenuLayout = view.findViewById(R.id.hamburgerMenuLayout);
        ImageView menuProfileIcon = view.findViewById(R.id.menuProfileIcon);
        ImageView menuCartIcon = view.findViewById(R.id.menuCartIcon);
        Switch menuThemeSwitch = view.findViewById(R.id.menuThemeSwitch);
        Button menuLogoutButton = view.findViewById(R.id.menuLogoutButton);

        hamburgerMenuIcon.setOnClickListener(v -> {
            if (hamburgerMenuLayout.getVisibility() == View.VISIBLE) {
                hamburgerMenuLayout.setVisibility(View.GONE);
            } else {
                hamburgerMenuLayout.setVisibility(View.VISIBLE);
            }
        });

        // Hide menu when clicking outside (optional, can be improved)
        view.setOnClickListener(v -> {
            if (hamburgerMenuLayout.getVisibility() == View.VISIBLE) {
                hamburgerMenuLayout.setVisibility(View.GONE);
            }
        });

        menuProfileIcon.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ProfileActivity.class));
        });
        menuCartIcon.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CartActivity.class));
        });

        // Theme toggle logic
        SharedPreferences prefs = requireContext().getSharedPreferences("settings", 0);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        menuThemeSwitch.setChecked(isDarkMode);
        menuThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
        });

        // Logout logic
        menuLogoutButton.setOnClickListener(v -> {
            FirebaseAuthManager authManager = new FirebaseAuthManager(requireContext());
            authManager.signOut();
            Intent intent = new Intent(getActivity(), AuthenticationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
            Toast.makeText(getActivity(), "Logged out", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
