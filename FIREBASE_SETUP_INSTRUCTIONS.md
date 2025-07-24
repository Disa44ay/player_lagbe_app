# Firebase Authentication Setup Instructions

This document outlines the complete Firebase Authentication implementation for the Player Lagbe Android app, including both Email/Password login and Google Sign-In functionality.

## 🔥 Firebase Configuration

### Project Details
- **Project ID**: player-lagbe-app
- **Package Name**: com.playerlagbe
- **App ID**: 1:590655616431:android:50bfd76371b7ad06e3eb5a
- **Web API Key**: AIzaSyBG_yt7R1sjNL3-ocokQ3LEWE7AdxmIkKc

### Required Setup Steps

#### 1. Firebase Console Configuration
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select the "player-lagbe-app" project
3. Navigate to **Authentication > Sign-in method**
4. Enable **Email/Password** authentication
5. Enable **Google** authentication
6. Add your app's SHA-1 fingerprint for Google Sign-In

#### 2. Getting SHA-1 Fingerprint
For debug builds:
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

For release builds:
```bash
keytool -list -v -keystore your_keystore_name -alias your_alias_name
```

#### 3. Web Client ID for Google Sign-In
⚠️ **IMPORTANT**: Update the `WEB_CLIENT_ID` in `FirebaseAuthManager.java`:

1. Go to Firebase Console > Project Settings > General
2. Under "Your apps" section, find the Web app
3. Copy the Web client ID
4. Replace the placeholder in `FirebaseAuthManager.java`:
```java
private static final String WEB_CLIENT_ID = "YOUR_ACTUAL_WEB_CLIENT_ID_HERE";
```

## 📱 Implementation Details

### Architecture Changes
- **Removed**: SharedPreferences-based authentication
- **Added**: Firebase Authentication with FirebaseAuthManager utility class
- **Updated**: All activities to use Firebase Auth state management

### Key Components

#### FirebaseAuthManager.java
Central authentication manager that handles:
- Email/password authentication
- Google Sign-In integration
- Password reset functionality
- Authentication state management
- User session handling

#### Updated Activities
1. **LauncherActivity**: Checks Firebase auth state on app start
2. **AuthenticationActivity**: Handles login with email/password and Google
3. **SignupActivity**: User registration with Firebase
4. **ForgotPasswordActivity**: Password reset via email
5. **MainActivity**: Displays user info and logout functionality

### Features Implemented

#### ✅ Email/Password Authentication
- User registration with email validation
- Password strength requirements (6+ chars, letters + numbers)
- Login with email and password
- Proper error handling with user-friendly messages

#### ✅ Google Sign-In
- Integrated with Firebase Authentication
- Uses GoogleSignInOptions with Web Client ID
- Handles authentication flow with proper callbacks
- Shows loading states during authentication

#### ✅ Password Reset
- Send password reset emails via Firebase
- Email validation before sending reset
- User feedback for success/failure

#### ✅ Authentication State Management
- Automatic redirection based on auth state
- Persistent login sessions
- Proper logout functionality
- Session validation on app resume

#### ✅ UI/UX Improvements
- Loading indicators during auth operations
- Disabled buttons during processing
- Clear error messages
- Proper navigation flow

## 🛠️ Dependencies Added

```gradle
// Firebase BoM for version management
implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
implementation("com.google.firebase:firebase-auth")
implementation("com.google.android.gms:play-services-auth:20.7.0")
```

## 🔒 Security Features

1. **Input Validation**: Email format, password strength
2. **Error Handling**: User-friendly error messages
3. **Session Management**: Automatic session validation
4. **Secure Logout**: Clears Firebase and Google sessions
5. **Network Security**: Uses HTTPS for all Firebase communication

## 📋 Testing Checklist

### Email/Password Authentication
- [ ] Register new user with valid email/password
- [ ] Login with registered credentials
- [ ] Test with invalid email format
- [ ] Test with weak password
- [ ] Test with mismatched passwords
- [ ] Test password reset functionality

### Google Sign-In
- [ ] Sign in with Google account
- [ ] Handle cancelled Google sign-in
- [ ] Test with different Google accounts
- [ ] Verify user information display

### Authentication Flow
- [ ] App launches to correct screen based on auth state
- [ ] Logout redirects to login screen
- [ ] Session persists across app restarts
- [ ] Proper navigation between activities

### Error Handling
- [ ] Network connectivity issues
- [ ] Invalid credentials
- [ ] Account already exists errors
- [ ] Firebase service errors

## 🚀 Deployment Notes

### Before Release
1. **Update Web Client ID**: Replace placeholder with actual Web Client ID
2. **Generate Release SHA-1**: Add production keystore SHA-1 to Firebase Console
3. **Test on Physical Device**: Ensure Google Sign-In works on real device
4. **Verify Email Sending**: Test password reset emails in production

### Firebase Console Settings
1. **Authorized Domains**: Ensure all required domains are whitelisted
2. **OAuth 2.0 Settings**: Verify redirect URIs and client IDs
3. **Email Templates**: Customize password reset email templates if needed

## 🐛 Troubleshooting

### Common Issues

#### Google Sign-In Fails
- Verify SHA-1 fingerprint is added to Firebase Console
- Check Web Client ID is correct
- Ensure Google Sign-In is enabled in Firebase Console

#### Email Authentication Fails
- Verify Email/Password is enabled in Firebase Console
- Check network connectivity
- Validate email format on client side

#### Build Errors
- Ensure google-services.json is in app/ directory
- Verify google-services plugin is applied
- Check Firebase dependencies are properly added

## 📞 Support

For issues related to Firebase setup:
1. Check Firebase Console project settings
2. Verify SHA-1 fingerprints
3. Review error logs in Android Studio
4. Consult Firebase documentation: https://firebase.google.com/docs/auth

---

**Last Updated**: [Current Date]
**Firebase SDK Version**: 32.3.1
**Play Services Auth Version**: 20.7.0