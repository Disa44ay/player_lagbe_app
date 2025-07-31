package com.playerlagbe;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class CartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Set up cart icon (disabled since we're already in cart)
        View topBarContainer = findViewById(R.id.topAppBar);
        if (topBarContainer != null) {
            ImageView cartIcon = topBarContainer.findViewById(R.id.cartIcon);
            if (cartIcon != null) {
                // Disable cart icon since we're already in the cart
                cartIcon.setClickable(false);
                cartIcon.setFocusable(false);
                cartIcon.setAlpha(0.5f); // Make it appear disabled
            }
        }
    }
}