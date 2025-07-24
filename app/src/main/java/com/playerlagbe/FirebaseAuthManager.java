package com.playerlagbe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Firebase Authentication Manager
 * Handles email/password and Google Sign-In authentication
 */
public class FirebaseAuthManager {

    private static final String TAG = "FirebaseAuthManager";
    public static final int RC_SIGN_IN = 9001;
    
    // Web Client ID for Google Sign-In - this will be read from google-services.json
    private static final String WEB_CLIENT_ID = "590655616431-your_actual_web_client_id.apps.googleusercontent.com";
    
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore mFirestore;
    private Context context;
    
    // Interface for authentication callbacks
    public interface AuthListener {
        void onAuthSuccess(FirebaseUser user);
        void onAuthFailure(String error);
        void onAuthLoading(boolean isLoading);
    }

    public FirebaseAuthManager(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        
        // Configure Google Sign-In - using default web client ID from google-services.json
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    /**
     * Check if user is currently signed in
     */
    public boolean isUserSignedIn() {
        return mAuth.getCurrentUser() != null;
    }

    /**
     * Get current Firebase user
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * Sign in with email/username and password
     * If input contains @, treat as email, otherwise treat as username
     */
    public void signInWithEmailPassword(String emailOrUsername, String password, AuthListener listener) {
        if (emailOrUsername.isEmpty() || password.isEmpty()) {
            listener.onAuthFailure("Please fill in all fields");
            return;
        }

        listener.onAuthLoading(true);
        
        // Check if input contains @ symbol (email) or not (username)
        if (emailOrUsername.contains("@")) {
            // Direct email login
            performEmailPasswordLogin(emailOrUsername, password, listener);
        } else {
            // Username login - first fetch email from Firestore
            fetchEmailFromUsername(emailOrUsername, password, listener);
        }
    }

    /**
     * Perform direct email/password login
     */
    private void performEmailPasswordLogin(String email, String password, AuthListener listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        listener.onAuthLoading(false);
                        
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            listener.onAuthSuccess(user);
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            String errorMessage = getAuthErrorMessage(task.getException());
                            listener.onAuthFailure(errorMessage);
                        }
                    }
                });
    }

    /**
     * Fetch email address from username in Firestore and then login
     */
    private void fetchEmailFromUsername(String username, String password, AuthListener listener) {
        Log.d(TAG, "Fetching email for username: " + username);
        
        mFirestore.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String email = document.getString("email");
                        
                        if (email != null && !email.isEmpty()) {
                            Log.d(TAG, "Found email for username: " + email);
                            performEmailPasswordLogin(email, password, listener);
                        } else {
                            listener.onAuthLoading(false);
                            listener.onAuthFailure("Username not found");
                        }
                    } else {
                        listener.onAuthLoading(false);
                        listener.onAuthFailure("Username not found");
                        Log.w(TAG, "Username query failed", task.getException());
                    }
                });
    }

    /**
     * Register user with email, password, and username
     */
    public void registerWithEmailPasswordAndUsername(String email, String password, String username, AuthListener listener) {
        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            listener.onAuthFailure("Please fill in all fields");
            return;
        }

        if (password.length() < 6) {
            listener.onAuthFailure("Password must be at least 6 characters");
            return;
        }

        listener.onAuthLoading(true);
        
        // First check if username already exists
        checkUsernameAvailability(username, isAvailable -> {
            if (!isAvailable) {
                listener.onAuthLoading(false);
                listener.onAuthFailure("Username already taken");
                return;
            }
            
            // Create user account
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                
                                if (user != null) {
                                    // Save user data to Firestore
                                    saveUserDataToFirestore(user.getUid(), email, username, listener);
                                } else {
                                    listener.onAuthLoading(false);
                                    listener.onAuthFailure("Failed to get user information");
                                }
                            } else {
                                listener.onAuthLoading(false);
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                String errorMessage = getAuthErrorMessage(task.getException());
                                listener.onAuthFailure(errorMessage);
                            }
                        }
                    });
        });
    }

    /**
     * Legacy method for backwards compatibility
     */
    public void registerWithEmailPassword(String email, String password, AuthListener listener) {
        registerWithEmailPasswordAndUsername(email, password, "", listener);
    }

    /**
     * Check if username is available
     */
    private void checkUsernameAvailability(String username, OnUsernameCheckListener listener) {
        mFirestore.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isAvailable = task.getResult().isEmpty();
                        listener.onUsernameChecked(isAvailable);
                    } else {
                        // If there's an error, assume username is not available for safety
                        listener.onUsernameChecked(false);
                    }
                });
    }

    /**
     * Save user data to Firestore
     */
    private void saveUserDataToFirestore(String uid, String email, String username, AuthListener listener) {
        // Create user data map
        java.util.Map<String, Object> userData = new java.util.HashMap<>();
        userData.put("email", email);
        userData.put("username", username);
        userData.put("createdAt", System.currentTimeMillis());
        
        mFirestore.collection("users").document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data saved successfully");
                    listener.onAuthLoading(false);
                    listener.onAuthSuccess(mAuth.getCurrentUser());
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error saving user data", e);
                    listener.onAuthLoading(false);
                    listener.onAuthFailure("Account created but failed to save user data");
                });
    }

    /**
     * Interface for username availability check
     */
    private interface OnUsernameCheckListener {
        void onUsernameChecked(boolean isAvailable);
    }

    /**
     * Start Google Sign-In intent
     */
    public Intent getGoogleSignInIntent() {
        return mGoogleSignInClient.getSignInIntent();
    }

    /**
     * Handle Google Sign-In result
     */
    public void handleGoogleSignInResult(Intent data, AuthListener listener) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        
        try {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.d(TAG, "Google Sign-In successful, account: " + account.getEmail());
            Log.d(TAG, "ID Token: " + (account.getIdToken() != null ? "Present" : "Null"));
            firebaseAuthWithGoogle(account.getIdToken(), listener);
        } catch (ApiException e) {
            // Google Sign In failed
            Log.w(TAG, "Google sign in failed with code: " + e.getStatusCode(), e);
            String errorMessage = getGoogleSignInErrorMessage(e.getStatusCode());
            listener.onAuthFailure(errorMessage);
        }
    }

    /**
     * Convert Google Sign-In error codes to user-friendly messages
     */
    private String getGoogleSignInErrorMessage(int statusCode) {
        switch (statusCode) {
            case 7: // NETWORK_ERROR
                return "Network error. Please check your internet connection.";
            case 8: // INTERNAL_ERROR
                return "Internal error occurred. Please try again.";
            case 10: // DEVELOPER_ERROR
                return "App configuration error. Please contact support.";
            case 12500: // SIGN_IN_CANCELLED
                return "Sign-in was cancelled.";
            case 12501: // SIGN_IN_CURRENTLY_IN_PROGRESS
                return "Sign-in already in progress.";
            case 12502: // SIGN_IN_FAILED
                return "Sign-in failed. Please try again.";
            default:
                return "Google Sign-In failed. Error code: " + statusCode;
        }
    }

    /**
     * Authenticate with Firebase using Google credentials
     */
    private void firebaseAuthWithGoogle(String idToken, AuthListener listener) {
        if (idToken == null) {
            listener.onAuthFailure("Failed to get Google ID token");
            return;
        }
        
        listener.onAuthLoading(true);
        
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        listener.onAuthLoading(false);
                        
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Firebase signInWithCredential: success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            
                            // For Google Sign-In users, save/update their data in Firestore
                            if (user != null && user.getDisplayName() != null) {
                                saveGoogleUserToFirestore(user, listener);
                            } else {
                                listener.onAuthSuccess(user);
                            }
                        } else {
                            Log.w(TAG, "Firebase signInWithCredential: failure", task.getException());
                            String errorMessage = getAuthErrorMessage(task.getException());
                            listener.onAuthFailure("Firebase authentication failed: " + errorMessage);
                        }
                    }
                });
    }

    /**
     * Save Google user data to Firestore
     */
    private void saveGoogleUserToFirestore(FirebaseUser user, AuthListener listener) {
        // Check if user document already exists
        mFirestore.collection("users").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (!document.exists()) {
                            // Create new user document for Google Sign-In user
                            java.util.Map<String, Object> userData = new java.util.HashMap<>();
                            userData.put("email", user.getEmail());
                            userData.put("username", generateUsernameFromEmail(user.getEmail()));
                            userData.put("displayName", user.getDisplayName());
                            userData.put("createdAt", System.currentTimeMillis());
                            userData.put("signInMethod", "google");
                            
                            mFirestore.collection("users").document(user.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Google user data saved successfully");
                                        listener.onAuthSuccess(user);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w(TAG, "Error saving Google user data", e);
                                        // Still proceed with login even if Firestore save fails
                                        listener.onAuthSuccess(user);
                                    });
                        } else {
                            // User already exists, just proceed with login
                            listener.onAuthSuccess(user);
                        }
                    } else {
                        // Error checking user existence, still proceed with login
                        Log.w(TAG, "Error checking user existence", task.getException());
                        listener.onAuthSuccess(user);
                    }
                });
    }

    /**
     * Generate a username from email for Google Sign-In users
     */
    private String generateUsernameFromEmail(String email) {
        if (email == null) return "user" + System.currentTimeMillis();
        
        String username = email.split("@")[0];
        // Remove any special characters and make it lowercase
        username = username.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        
        // If too short, append timestamp
        if (username.length() < 3) {
            username += System.currentTimeMillis();
        }
        
        return username;
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email, AuthListener listener) {
        if (email.isEmpty()) {
            listener.onAuthFailure("Please enter your email address");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            listener.onAuthFailure("Please enter a valid email address");
            return;
        }

        Log.d(TAG, "Sending password reset email to: " + email);
        listener.onAuthLoading(true);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        listener.onAuthLoading(false);
                        
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Password reset email sent successfully to: " + email);
                            listener.onAuthSuccess(null);
                        } else {
                            Log.w(TAG, "Failed to send password reset email", task.getException());
                            String errorMessage = getAuthErrorMessage(task.getException());
                            listener.onAuthFailure("Failed to send reset email: " + errorMessage);
                        }
                    }
                });
    }

    /**
     * Sign out user
     */
    public void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut();
        Log.d(TAG, "User signed out");
    }

    /**
     * Convert Firebase Auth exceptions to user-friendly messages
     */
    private String getAuthErrorMessage(Exception exception) {
        if (exception == null) return "Unknown error occurred";
        
        String errorCode = exception.getMessage();
        if (errorCode == null) return "Authentication failed";
        
        if (errorCode.contains("user-not-found")) {
            return "No account found with this email address";
        } else if (errorCode.contains("wrong-password")) {
            return "Incorrect password";
        } else if (errorCode.contains("invalid-email")) {
            return "Invalid email address";
        } else if (errorCode.contains("email-already-in-use")) {
            return "An account with this email already exists";
        } else if (errorCode.contains("weak-password")) {
            return "Password is too weak";
        } else if (errorCode.contains("network-request-failed")) {
            return "Network error. Please check your connection";
        } else {
            return "Authentication failed. Please try again";
        }
    }
}