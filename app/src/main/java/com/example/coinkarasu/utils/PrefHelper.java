package com.example.coinkarasu.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class PrefHelper {

    private static final int DEFAULT_SYNC_INTERVAL = 10000;

    public static int getSyncInterval(Activity activity) {
        SharedPreferences pref = getPref(activity);
        if (pref == null) {
            return DEFAULT_SYNC_INTERVAL;
        }
        String value = pref.getString("pref_sync_frequency", String.valueOf(DEFAULT_SYNC_INTERVAL));
        int interval = Integer.valueOf(value);

        if (interval < 5000) {
            Log.d("INVALID_Interval", "" + interval);
            interval = DEFAULT_SYNC_INTERVAL;
        }

        return interval;
    }

    public static boolean isAnimEnabled(Activity activity) {
        SharedPreferences pref = getPref(activity);
        if (pref == null) {
            return false;
        }
        return pref.getBoolean("pref_enable_price_anim", false);
    }

    public static void setToSymbol(Activity activity, String toSymbol) {
        SharedPreferences pref = getPref(activity);
        if (pref == null) {
            return;
        }
        SharedPreferences.Editor edit = pref.edit();
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
