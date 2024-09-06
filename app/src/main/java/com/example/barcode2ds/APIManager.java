package com.example.barcode2ds;

import android.content.Context;
import android.content.SharedPreferences;

public class APIManager {
    private static final String PREF_NAME = "APIPrefs";
    private static final String API_URL_KEY = "api_url";

    public static void saveApiUrl(Context context, String url) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(API_URL_KEY, url);
        editor.apply();
    }

    public static String getApiUrl(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(API_URL_KEY, "");
    }
}