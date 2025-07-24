package com.playerlagbe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
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

/**
 * Firebase Authentication Manager
 * Handles email/password and Google Sign-In authentication
 */
public class FirebaseAuthManager {

    private static final String TAG = "FirebaseAuthManager";
    public static final int RC_SIGN_IN = 9001;
    
    // Web Client ID for Google Sign-In - replace with actual value from Firebase Console
    private static final String WEB_CLIENT_ID = "590655616431-webdefghijklmnopqrstuvwxyz78901.apps.googleusercontent.com";
    
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
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
        
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
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
     * Sign in with email and password
     */
    public void signInWithEmailPassword(String email, String password, AuthListener listener) {
        if (email.isEmpty() || password.isEmpty()) {
            listener.onAuthFailure("Please fill in all fields");
            return;
        }

        listener.onAuthLoading(true);
        
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
     * Register user with email and password
     */
    public void registerWithEmailPassword(String email, String password, AuthListener listener) {
        if (email.isEmpty() || password.isEmpty()) {
            listener.onAuthFailure("Please fill in all fields");
            return;
        }

        if (password.length() < 6) {
            listener.onAuthFailure("Password must be at least 6 characters");
            return;
        }

        listener.onAuthLoading(true);
        
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        listener.onAuthLoading(false);
                        
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            listener.onAuthSuccess(user);
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            String errorMessage = getAuthErrorMessage(task.getException());
                            listener.onAuthFailure(errorMessage);
                        }
                    }
                });
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
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
            firebaseAuthWithGoogle(account.getIdToken(), listener);
        } catch (ApiException e) {
            // Google Sign In failed
            Log.w(TAG, "Google sign in failed", e);
            listener.onAuthFailure("Google sign in failed: " + e.getMessage());
        }
    }

    /**
     * Authenticate with Firebase using Google credentials
     */
    private void firebaseAuthWithGoogle(String idToken, AuthListener listener) {
        listener.onAuthLoading(true);
        
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        listener.onAuthLoading(false);
                        
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            listener.onAuthSuccess(user);
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String errorMessage = getAuthErrorMessage(task.getException());
                            listener.onAuthFailure(errorMessage);
                        }
                    }
                });
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email, AuthListener listener) {
        if (email.isEmpty()) {
            listener.onAuthFailure("Please enter your email address");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            listener.onAuthSuccess(null);
                        } else {
                            Log.w(TAG, "sendPasswordResetEmail:failure", task.getException());
                            String errorMessage = getAuthErrorMessage(task.getException());
                            listener.onAuthFailure(errorMessage);
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