package com.train.track.controller.util;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtility {

    public static void setStringPreference(Context context, String prefName, String key, String value) {
        SharedPreferences userPref = context.getSharedPreferences(prefName, MODE_PRIVATE);
        SharedPreferences.Editor editor = userPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getStringPreference(Context context,String prefName, String key, String defaultValue) {
        SharedPreferences usePref = context.getSharedPreferences(prefName, MODE_PRIVATE);
        return usePref.getString(key, defaultValue);
    }
}
