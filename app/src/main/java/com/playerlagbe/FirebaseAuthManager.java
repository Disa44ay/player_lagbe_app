package com.playerlagbe;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Patterns;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.*;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import java.util.*;

public class FirebaseAuthManager {

    private static final String TAG = "FirebaseAuthManager";
    public static final int RC_SIGN_IN = 9001;

    private final FirebaseAuth mAuth;
    private final GoogleSignInClient mGoogleSignInClient;
    private final FirebaseFirestore mFirestore;
    private final Context context;

    public interface AuthListener {
        void onAuthSuccess(FirebaseUser user);
        void onAuthFailure(String error);
        void onAuthLoading(boolean isLoading);
    }

    public FirebaseAuthManager(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(context,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail().build());
    }

    public boolean isUserSignedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void signIn(String input, String password, AuthListener listener) {
        if (input.isEmpty() || password.isEmpty()) {
            listener.onAuthFailure("Fields cannot be empty");
            return;
        }
        listener.onAuthLoading(true);
        if (input.contains("@")) loginWithEmail(input, password, listener);
        else fetchEmailByUsername(input, email -> loginWithEmail(email, password, listener), listener);
    }

    private void loginWithEmail(String email, String password, AuthListener listener) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            listener.onAuthLoading(false);
            if (task.isSuccessful()) listener.onAuthSuccess(mAuth.getCurrentUser());
            else listener.onAuthFailure(getAuthErrorMessage(task.getException()));
        });
    }

    private void fetchEmailByUsername(String username, OnEmailFetched callback, AuthListener listener) {
        Log.d("FIRESTORE", "Querying Firestore for username: " + username);

        mFirestore.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FIRESTORE", "Query successful. Documents found: " + task.getResult().size());

                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                            Log.d("FIRESTORE", "User document: " + doc.getData());
                            String email = doc.getString("email");
                            Log.d("FIRESTORE", "Extracted email: " + email);
                            if (email != null && !email.isEmpty()) {
                                // Start loading for the actual email login
                                listener.onAuthLoading(true);
                                callback.onFetched(email);
                            } else {
                                listener.onAuthLoading(false);
                                listener.onAuthFailure("Email not found for username: " + username);
                            }
                        } else {
                            Log.w("FIRESTORE", "No user found with username: " + username);
                            listener.onAuthLoading(false);
                            listener.onAuthFailure("Username '" + username + "' not found. Please check spelling or use your email to login.");
                        }
                    } else {
                        Exception e = task.getException();
                        Log.e("FIRESTORE", "Firestore query failed", e);
                        listener.onAuthLoading(false);
                        if (e != null && e.getMessage() != null && e.getMessage().contains("PERMISSION_DENIED")) {
                            listener.onAuthFailure("Unable to verify username. Please try logging in with your email address instead.");
                        } else {
                            listener.onAuthFailure("Connection error. Please check your internet and try again.");
                        }
                    }
                });
    }

    public void register(String email, String password, String username, AuthListener listener) {
        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            listener.onAuthFailure("All fields required"); return;
        }
        if (password.length() < 6) {
            listener.onAuthFailure("Password too short"); return;
        }
        listener.onAuthLoading(true);
        checkUsername(username, available -> {
            if (!available) {
                listener.onAuthLoading(false);
                listener.onAuthFailure("Username taken");
                return;
            }
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            saveUser(mAuth.getCurrentUser(), email, username, listener);
                        } else {
                            listener.onAuthLoading(false);
                            listener.onAuthFailure(getAuthErrorMessage(task.getException()));
                        }
                    });
        });
    }

    private void checkUsername(String username, OnUsernameChecked listener) {
        mFirestore.collection("users").whereEqualTo("username", username).limit(1).get()
                .addOnCompleteListener(task -> listener.onChecked(task.isSuccessful() && task.getResult().isEmpty()));
    }

    private void saveUser(FirebaseUser user, String email, String username, AuthListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("username", username);
        data.put("createdAt", System.currentTimeMillis());
        mFirestore.collection("users").document(user.getUid()).set(data)
                .addOnSuccessListener(aVoid -> listener.onAuthSuccess(user))
                .addOnFailureListener(e -> listener.onAuthFailure("Saved auth but failed Firestore"));
    }

    public Intent getGoogleSignInIntent() {
        return mGoogleSignInClient.getSignInIntent();
    }

    public void handleGoogleSignInResult(Intent data, AuthListener listener) {
        try {
            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken(), listener);
        } catch (ApiException e) {
            listener.onAuthFailure("Google Sign-In failed: " + e.getStatusCode());
        }
    }

    private void firebaseAuthWithGoogle(String idToken, AuthListener listener) {
        if (idToken == null) {
            listener.onAuthFailure("No ID token"); return;
        }
        listener.onAuthLoading(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            listener.onAuthLoading(false);
            if (task.isSuccessful()) saveGoogleUser(mAuth.getCurrentUser(), listener);
            else listener.onAuthFailure(getAuthErrorMessage(task.getException()));
        });
    }

    private void saveGoogleUser(FirebaseUser user, AuthListener listener) {
        DocumentReference ref = mFirestore.collection("users").document(user.getUid());
        ref.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                Map<String, Object> data = new HashMap<>();
                data.put("email", user.getEmail());
                data.put("username", user.getEmail().split("@")[0]);
                data.put("displayName", user.getDisplayName());
                data.put("createdAt", System.currentTimeMillis());
                data.put("signInMethod", "google");
                ref.set(data);
            }
            listener.onAuthSuccess(user);
        });
    }

    public void sendPasswordResetEmail(String email, AuthListener listener) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            listener.onAuthFailure("Invalid email"); return;
        }
        listener.onAuthLoading(true);
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            listener.onAuthLoading(false);
            if (task.isSuccessful()) listener.onAuthSuccess(null);
            else listener.onAuthFailure(getAuthErrorMessage(task.getException()));
        });
    }

    public void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut();
    }

    private String getAuthErrorMessage(Exception e) {
        if (e == null || e.getMessage() == null) return "Auth failed";
        String msg = e.getMessage();
        if (msg.contains("user-not-found")) return "User not found";
        if (msg.contains("wrong-password")) return "Wrong password";
        if (msg.contains("email-already-in-use")) return "Email in use";
        if (msg.contains("weak-password")) return "Weak password";
        return "Error: " + msg;
    }

    private interface OnEmailFetched { void onFetched(String email); }
    private interface OnUsernameChecked { void onChecked(boolean available); }
}
