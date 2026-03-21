package com.vaultpool.customer.data.local.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;

/**
 * Secure preferences manager for storing sensitive data like tokens.
 */
public class PreferencesManager {

    private static final String PREFS_NAME = "vaultpool_secure_prefs";
    private static final String KEY_FIREBASE_TOKEN = "firebase_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ROLES = "user_roles";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences encryptedPrefs;

    public MasterKey masterKey;

    public PreferencesManager(Context context) {
        try {
            masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to create encrypted shared preferences", e);
        }
    }

    // Firebase Token
    public void saveFirebaseToken(String token) {
        encryptedPrefs.edit().putString(KEY_FIREBASE_TOKEN, token).apply();
    }

    public String getFirebaseToken() {
        return encryptedPrefs.getString(KEY_FIREBASE_TOKEN, null);
    }

    public void clearFirebaseToken() {
        encryptedPrefs.edit().remove(KEY_FIREBASE_TOKEN).apply();
    }

    // User ID
    public void saveUserId(String userId) {
        encryptedPrefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return encryptedPrefs.getString(KEY_USER_ID, null);
    }

    // User Email
    public void saveUserEmail(String email) {
        encryptedPrefs.edit().putString(KEY_USER_EMAIL, email).apply();
    }

    public String getUserEmail() {
        return encryptedPrefs.getString(KEY_USER_EMAIL, null);
    }

    // User Name
    public void saveUserName(String name) {
        encryptedPrefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    public String getUserName() {
        return encryptedPrefs.getString(KEY_USER_NAME, null);
    }

    // User Roles
    public void saveUserRoles(Set<String> roles) {
        encryptedPrefs.edit().putStringSet(KEY_USER_ROLES, roles).apply();
    }

    public Set<String> getUserRoles() {
        return encryptedPrefs.getStringSet(KEY_USER_ROLES, new HashSet<>());
    }

    public boolean isStaff() {
        Set<String> roles = getUserRoles();
        return roles.contains("STAFF") || roles.contains("ADMIN");
    }

    // Login State
    public void setLoggedIn(boolean isLoggedIn) {
        encryptedPrefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply();
    }

    public boolean isLoggedIn() {
        return encryptedPrefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Clear all data (logout)
    public void clearAll() {
        encryptedPrefs.edit().clear().apply();
    }
}
