package com.example.cooking.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.cooking.AppConstants;
import com.example.cooking.model.AuthResponse;

public class SharedPreferencesManager {
    private static SharedPreferences prefs;

    public static void initialize(Context context) {
        prefs = context.getSharedPreferences(
                AppConstants.SHARED_PREFS_NAME,
                Context.MODE_PRIVATE
        );
    }

    public static void saveToken(String token) {
        prefs.edit().putString(AppConstants.TOKEN_KEY, token).apply();
    }

    public static String getToken() {
        return prefs.getString(AppConstants.TOKEN_KEY, null);
    }

    public static void saveUserData(AuthResponse response) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(AppConstants.USER_ID_KEY, response.getUserId());
        editor.putString(AppConstants.USER_EMAIL_KEY, response.getEmail());
        editor.putString(AppConstants.USER_NAME_KEY, response.getFullName());
        editor.putString(AppConstants.USER_ROLE_KEY, response.getRole());
        editor.apply();
    }

    public static void clearUserData() {
        prefs.edit().clear().apply();
    }

    public static boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }
}
