package com.playerlagbe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

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
    }
}