package com.example.coinkarasu.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class PrefHelper {

    public static int getSyncInterval(Activity activity) {
        String value = getPref(activity).getString("pref_sync_frequency", "10000");
        int interval = Integer.valueOf(value);

        if (interval < 5000) {
            Log.d("INVALID_Interval", "" + interval);
            interval = 10000;
        }

        return interval;
    }

    public static void setToSymbol(Activity activity, String toSymbol) {
        SharedPreferences.Editor edit = getPref(activity).edit();
        edit.putString("pref_currency", toSymbol);
        edit.apply();
    }

    public static String getToSymbol(Activity activity) {
        SharedPreferences pref = getPref(activity);
        if (pref == null) {
            return null;
        }
        return pref.getString("pref_currency", "JPY");
    }

    private static SharedPreferences getPref(Activity activity) {
        if (activity == null) {
            return null;
        }
        return PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
    }
}
