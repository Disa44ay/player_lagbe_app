package com.playerlagbe;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {

    SharedPreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefsManager = new SharedPreferencesManager(this);

        if (prefsManager.isLoggedIn()) {
            // Go to MainActivity (home screen)
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // Go to AuthenticationActivity (login/signup)
            startActivity(new Intent(this, AuthenticationActivity.class));
        }

        finish();
    }
}
