package com.example.cooking.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private static final String PREF_NAME = "cooking_app_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_IS_ADMIN = "is_admin";
    private static final String KEY_TOKEN_TYPE = "token_type";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private static SharedPreferencesManager instance;
    private SharedPreferences prefs;


    private SharedPreferencesManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }


    public static void init(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesManager(context);
        }
    }


    private static SharedPreferencesManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SharedPreferencesManager не инициализирован. Вызовите init() в Application.onCreate");
        }
        return instance;
    }


    public static void setUserId(Long userId) {
        getInstance().prefs.edit().putLong(KEY_USER_ID, userId != null ? userId : 0).apply();
    }

    public static Long getUserId() {
        return getInstance().prefs.getLong(KEY_USER_ID, 0);
    }

    public static void setUserName(String userName) {
        getInstance().prefs.edit().putString(KEY_USER_NAME, userName).apply();
    }

    public static String getUserName() {
        return getInstance().prefs.getString(KEY_USER_NAME, "");
    }

    public static void setUserEmail(String email) {
        getInstance().prefs.edit().putString(KEY_USER_EMAIL, email).apply();
    }


    public static void setTokenType(String tokenType) {
        getInstance().prefs.edit().putString(KEY_TOKEN_TYPE, tokenType).apply();
    }

    public static String getTokenType() {
        return getInstance().prefs.getString(KEY_TOKEN_TYPE, "Bearer"); // по умолчанию Bearer
    }

    public static String getUserEmail() {
        return getInstance().prefs.getString(KEY_USER_EMAIL, "");
    }

    public static void setToken(String token) {
        getInstance().prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public static String getToken() {
        return getInstance().prefs.getString(KEY_TOKEN, null);
    }

    public static void setAdmin(boolean isAdmin) {
        getInstance().prefs.edit().putBoolean(KEY_IS_ADMIN, isAdmin).apply();
    }

    public static boolean isAdmin() {
        return getInstance().prefs.getBoolean(KEY_IS_ADMIN, false);
    }

    public static void clear() {
        getInstance().prefs.edit().clear().apply();
    }
    public static void setLoggedIn(boolean isLoggedIn) {
        getInstance().prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply();
    }

    public static boolean isLoggedIn() {
        return getInstance().prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    public static boolean isInitialized() {
        return instance != null;
    }


    public static void clearAuth() {
        getInstance().prefs.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_TOKEN_TYPE)
                .remove(KEY_IS_LOGGED_IN)
                .apply();
    }
}