package com.playerlagbe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Set up cart icon click listener
        View topBarContainer = findViewById(R.id.topAppBar);
        if (topBarContainer != null) {
            ImageView cartIcon = topBarContainer.findViewById(R.id.cartIcon);
            if (cartIcon != null) {
                cartIcon.setOnClickListener(v -> {
                    Intent intent = new Intent(ProfileActivity.this, CartActivity.class);
                    startActivity(intent);
                });
            }
        }
    }
}