package com.example.heartapp;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String PREF_NAME = "HeartAppPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_BASELINE = "baseline";

    public static void saveUserInfo(Context context, String username, String phone) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USERNAME, username).putString(KEY_PHONE, phone).apply();
    }

    public static void saveBaseline(Context context, float baseline) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putFloat(KEY_BASELINE, baseline).apply();
    }

    public static String getUsername(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_USERNAME, "");
    }

    public static String getPhone(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_PHONE, "");
    }

    public static float getBaseline(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getFloat(KEY_BASELINE, 0f);
    }
}
