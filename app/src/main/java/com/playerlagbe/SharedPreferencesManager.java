package com.playerlagbe;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static final String PREF_NAME = "PlayerLagbePrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_CURRENT_USER = "currentUser";
    private static final String KEY_USER_PREFIX = "user_";   // stores password by username key
    private static final String KEY_EMAIL_PREFIX = "email_"; // stores email by username key

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Save new user credentials (username, password, email)
    public void saveUser(String username, String password, String email) {
        editor.putString(KEY_USER_PREFIX + username, password);
        editor.putString(KEY_EMAIL_PREFIX + username, email);
        editor.apply();
    }

    // Check if user exists by username
    public boolean userExists(String username) {
        return sharedPreferences.contains(KEY_USER_PREFIX + username);
    }

    // Validate user login by matching username and password
    public boolean validateUser(String username, String password) {
        String storedPassword = sharedPreferences.getString(KEY_USER_PREFIX + username, null);
        return storedPassword != null && storedPassword.equals(password);
    }

    // Update password for an existing user
    public void updatePassword(String username, String newPassword) {
        editor.putString(KEY_USER_PREFIX + username, newPassword);
        editor.apply();
    }

    // Set login status (true if logged in)
    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    // Check if currently logged in
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Store current logged in username
    public void setCurrentUser(String username) {
        editor.putString(KEY_CURRENT_USER, username);
        editor.apply();
    }

    // Get current logged in username
    public String getCurrentUser() {
        return sharedPreferences.getString(KEY_CURRENT_USER, null);
    }

    // Clear login info to logout
    public void logout() {
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.putString(KEY_CURRENT_USER, null);
        editor.apply();
    }

    // Optionally get stored email of user
    public String getUserEmail(String username) {
        return sharedPreferences.getString(KEY_EMAIL_PREFIX + username, null);
    }
}
