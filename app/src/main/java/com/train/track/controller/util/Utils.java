package com.train.track.controller.util;

import static com.train.track.controller.util.Utils.PrefConstants.APP_PREFS;

import android.widget.Toast;

import com.train.track.controller.AppController;

public class Utils {

    public static String BLUETOOTH_OBJECT = "bluetooth_object";

    public static void showToast(String msg) {
        Toast.makeText(AppController.getInstance().getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public static void setPrefs(String key, String value) {
        PreferenceUtility.setStringPreference(AppController.getInstance().getContext(), APP_PREFS, key, value);
    }

    public static String getStringPrefs(String key) {
        return PreferenceUtility.getStringPreference(AppController.getInstance().getContext(), APP_PREFS, key, "");
    }

    public static class PrefConstants {
        public static String APP_PREFS = "app_prefs";

        public static String PREF_USER_EMAIL = "pref_user_email";
    }
}
