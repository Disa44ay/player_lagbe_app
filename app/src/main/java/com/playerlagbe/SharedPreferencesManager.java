package com.playerlagbe;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private static final String PREF_NAME = "PlayerLagbePrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_CURRENT_USER = "currentUser";
    private static final String KEY_USER_PREFIX = "user_";
    private static final String KEY_EMAIL_PREFIX = "email_";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveUser(String username, String password, String email) {
        editor.putString(KEY_USER_PREFIX + username, password);
        editor.putString(KEY_EMAIL_PREFIX + username, email);
        editor.apply();
    }

    public boolean validateUser(String username, String password) {
        String storedPassword = sharedPreferences.getString(KEY_USER_PREFIX + username, null);
        return storedPassword != null && storedPassword.equals(password);
    }

    public boolean userExists(String username) {
        return sharedPreferences.contains(KEY_USER_PREFIX + username);
    }

    public void updatePassword(String username, String newPassword) {
        editor.putString(KEY_USER_PREFIX + username, newPassword);
        editor.apply();
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setCurrentUser(String username) {
        editor.putString(KEY_CURRENT_USER, username);
        editor.apply();
    }

    public String getCurrentUser() {
        return sharedPreferences.getString(KEY_CURRENT_USER, null);
    }

    public void logout() {
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.putString(KEY_CURRENT_USER, null);
        editor.apply();
    }

    public String getUserEmail(String username) {
        return sharedPreferences.getString(KEY_EMAIL_PREFIX + username, null);
    }
}
