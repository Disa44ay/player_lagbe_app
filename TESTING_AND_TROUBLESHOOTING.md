# Testing and Troubleshooting Guide

## Overview
This document provides step-by-step testing instructions and troubleshooting for the fixes implemented for the three critical authentication issues.

## 🔧 Fixes Implemented

### 1. Google Sign-In Integration
**Problems Fixed:**
- Corrected Web Client ID configuration to use `google-services.json`
- Added proper error handling and logging
- Implemented automatic user data storage in Firestore for Google users
- Added progress indicators and better UI feedback

**Key Changes:**
- Updated `FirebaseAuthManager.java` with proper Google Sign-In configuration
- Added `default_web_client_id` string resource
- Enhanced error messages for different Google Sign-In failure scenarios
- Automatic username generation for Google users

### 2. Forgot Password Functionality
**Problems Fixed:**
- Fixed layout to show proper password reset form (email input only)
- Added email validation before sending reset email
- Improved success/error messaging with Toast notifications
- Added progress bar for better user experience

**Key Changes:**
- Updated `activity_forgot_password.xml` layout
- Enhanced `ForgotPasswordActivity.java` with proper validation
- Added detailed logging for debugging

### 3. Username-Based Login
**Problems Fixed:**
- Added Firestore dependency for username storage
- Implemented username-to-email mapping logic
- Updated signup process to store usernames in Firestore
- Enhanced login to support both email and username input

**Key Changes:**
- Added Firestore integration in `FirebaseAuthManager.java`
- Updated `SignupActivity.java` to save usernames
- Modified login logic in `AuthenticationActivity.java`
- Added username availability checking

## 🧪 Testing Instructions

### Google Sign-In Testing

1. **Prerequisites:**
   - Ensure SHA-1 fingerprint is added to Firebase Console
   - Verify `google-services.json` is up to date
   - Check that package name matches Firebase project

2. **Test Steps:**
   ```
   1. Open the app
   2. Tap "Sign in with Google"
   3. Select a Google account
   4. Verify successful login and navigation to MainActivity
   5. Check Logcat for "Google Sign-In successful" messages
   ```

3. **Expected Behavior:**
   - Google Sign-In flow should open
   - User should be able to select an account
   - App should navigate to MainActivity after successful login
   - User data should be saved to Firestore automatically

4. **Troubleshooting:**
   ```bash
   # Check SHA-1 fingerprint
   ./gradlew signingReport
   
   # Look for these logs:
   # D/FirebaseAuthManager: Google Sign-In successful, account: user@gmail.com
   # D/FirebaseAuthManager: ID Token: Present
   # D/FirebaseAuthManager: Firebase signInWithCredential: success
   ```

### Forgot Password Testing

1. **Test Steps:**
   ```
   1. Open the app
   2. Tap "Forgot Password?"
   3. Enter a valid email address
   4. Tap "Send Reset Email"
   5. Check email inbox for reset link
   ```

2. **Expected Behavior:**
   - Email validation should work correctly
   - Success message should appear: "Password reset email sent! Check your inbox."
   - User should receive email within a few minutes
   - Progress bar should show during the process

3. **Troubleshooting:**
   ```bash
   # Look for these logs:
   # D/FirebaseAuthManager: Sending password reset email to: user@example.com
   # D/FirebaseAuthManager: Password reset email sent successfully to: user@example.com
   
   # If emails aren't received:
   # - Check spam folder
   # - Verify email exists in Firebase Auth
   # - Check Firebase Auth settings in console
   ```

### Username Login Testing

1. **Test User Creation:**
   ```
   1. Open the app
   2. Tap "Don't have an account? Sign up"
   3. Fill in all fields including username
   4. Create account successfully
   ```

2. **Test Username Login:**
   ```
   1. Sign out if logged in
   2. Enter username (not email) in login field
   3. Enter password
   4. Tap "Login"
   5. Verify successful login
   ```

3. **Test Email Login:**
   ```
   1. Sign out if logged in
   2. Enter email address in login field
   3. Enter password
   4. Tap "Login"
   5. Verify successful login
   ```

4. **Expected Behavior:**
   - Both username and email should work for login
   - App should automatically detect if input is email (contains @) or username
   - User data should be stored in Firestore with both email and username

5. **Troubleshooting:**
   ```bash
   # Look for these logs:
   # D/FirebaseAuthManager: Fetching email for username: testuser
   # D/FirebaseAuthManager: Found email for username: user@example.com
   # D/FirebaseAuthManager: signInWithEmail:success
   
   # Check Firestore console for user documents:
   # Collection: users
   # Document ID: {user_uid}
   # Fields: email, username, createdAt
   ```

## 🐛 Common Issues and Solutions

### Google Sign-In Issues

**Issue: "App configuration error" or "DEVELOPER_ERROR"**
- Solution: Verify SHA-1 fingerprint in Firebase Console
- Check that `google-services.json` is latest version
- Ensure package name matches exactly

**Issue: "Sign-in failed" or "SIGN_IN_FAILED"**
- Solution: Check internet connection
- Verify Google Play Services is updated
- Try clearing app data and cache

**Issue: "Failed to get Google ID token"**
- Solution: Check Web Client ID configuration
- Verify OAuth 2.0 client IDs in Google Cloud Console
- Ensure `default_web_client_id` string resource is correct

### Forgot Password Issues

**Issue: No reset email received**
- Solution: Check spam folder
- Verify email address exists in Firebase Auth
- Check Firebase Auth email settings
- Wait up to 10 minutes for delivery

**Issue: "Invalid email address" error**
- Solution: Ensure proper email format (contains @ and domain)
- Check for extra spaces in input

### Username Login Issues

**Issue: "Username not found" error**
- Solution: Verify username was saved during signup
- Check Firestore rules allow reading users collection
- Ensure username is exact match (case-sensitive)

**Issue: Firestore permission denied**
- Solution: Update Firestore security rules:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /users/{document=**} {
      allow read: if request.auth != null;
    }
  }
}
```

## 🔍 Debugging Commands

```bash
# Build and install the app
./gradlew installDebug

# View logs
adb logcat | grep -E "(FirebaseAuthManager|AuthenticationActivity|ForgotPasswordActivity|SignupActivity)"

# Check Firebase Auth users
# Go to Firebase Console > Authentication > Users

# Check Firestore data
# Go to Firebase Console > Firestore Database > users collection

# Generate debug keystore SHA-1
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

## 📝 Next Steps

1. **Test each functionality thoroughly**
2. **Monitor logs during testing**
3. **Verify Firestore security rules**
4. **Update SHA-1 keys if needed**
5. **Test on different devices/emulators**

## 🚀 Production Checklist

- [ ] Add release keystore SHA-1 to Firebase Console
- [ ] Update Firestore security rules for production
- [ ] Test with production `google-services.json`
- [ ] Verify email deliverability settings
- [ ] Set up proper error monitoring/analytics

## Contact Information

If you encounter any issues not covered in this guide, please check:
1. Firebase Console error logs
2. Android Studio Logcat output
3. Firestore security rules
4. Google Play Services version on test device