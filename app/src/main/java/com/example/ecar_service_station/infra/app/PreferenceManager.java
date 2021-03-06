package com.example.ecar_service_station.infra.app;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    public static final String PREFERENCES_NAME = "activity-preferences";

    private static final String DEFAULT_VALUE_STRING = "";
    private static final int DEFAULT_VALUE_INT = -1;
    private static final long DEFAULT_VALUE_LONG = -1L;
    private static final boolean DEFAULT_VALUE_BOOLEAN = false;

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static void setString(Context context, String key, String value) {
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(key, value);
        editor.apply();
    }

    public static void setInt(Context context, String key, int value) {
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(key, value);
        editor.apply();
    }

    public static void setLong(Context context, String key, long value) {
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putLong(key, value);
        editor.apply();
    }

    public static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key) {
        SharedPreferences preferences = getPreferences(context);

        return preferences.getString(key, DEFAULT_VALUE_STRING);
    }

    public static int getInt(Context context, String key) {
        SharedPreferences preferences = getPreferences(context);

        return preferences.getInt(key, DEFAULT_VALUE_INT);
    }

    public static long getLong(Context context, String key) {
        SharedPreferences preferences = getPreferences(context);

        return preferences.getLong(key, DEFAULT_VALUE_LONG);
    }

    public static boolean getBoolean(Context context, String key) {
        SharedPreferences preferences = getPreferences(context);

        return preferences.getBoolean(key, DEFAULT_VALUE_BOOLEAN);
    }
}
