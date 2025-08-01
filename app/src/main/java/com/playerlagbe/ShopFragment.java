package com.playerlagbe;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ShopFragment extends Fragment {

    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_shop, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupHamburgerMenu();
    }

    private void setupHamburgerMenu() {
        ImageView hamburgerMenuIcon = rootView.findViewById(R.id.hamburgerMenuIcon);
        if (hamburgerMenuIcon != null) {
            hamburgerMenuIcon.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(getContext(), hamburgerMenuIcon);
                popup.getMenuInflater().inflate(R.menu.menu_hamburger, popup.getMenu());
                
                popup.getMenu().findItem(R.id.menu_profile).setIcon(R.drawable.ic_profile);
                popup.getMenu().findItem(R.id.menu_cart).setIcon(R.drawable.ic_cart);
                popup.getMenu().findItem(R.id.menu_theme).setIcon(R.drawable.ic_theme_toggle);
                popup.getMenu().findItem(R.id.menu_logout).setIcon(R.drawable.ic_logout);
                
                SharedPreferences prefs = getContext().getSharedPreferences("settings", 0);
                boolean isDarkMode = prefs.getBoolean("dark_mode", false);
                popup.getMenu().findItem(R.id.menu_theme).setTitle(isDarkMode ? "Light Mode" : "Dark Mode");
                
                popup.setOnMenuItemClickListener(item -> {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    if (mainActivity != null) {
                        mainActivity.handleHamburgerMenuAction(item.getItemId());
                    }
                    return true;
                });
                popup.show();
            });
        }
    }
}