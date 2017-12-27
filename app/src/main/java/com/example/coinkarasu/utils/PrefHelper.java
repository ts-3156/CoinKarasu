package com.example.coinkarasu.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.coinkarasu.activities.NavigationKind;

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
        return pref.getBoolean("pref_enable_price_anim", true);
    }

    public static boolean isDownloadIconEnabled(Activity activity) {
        SharedPreferences pref = getPref(activity);
        if (pref == null) {
            return false;
        }
        return pref.getBoolean("pref_enable_download_icon", true);
    }

    public static void saveToSymbol(Activity activity, String toSymbol) {
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

    public static boolean isVisibleTab(Context context, NavigationKind kind) {
        SharedPreferences pref = getPref(context);
        if (pref == null) {
            return false;
        }
        return pref.getBoolean("pref_is_visible_tab_" + kind.name(), kind.defaultVisibility());
    }

    private static void saveTabVisibility(Context context, NavigationKind kind, boolean flag) {
        if (!kind.isHideable() || !kind.isShowable()) {
            return;
        }
        SharedPreferences pref = getPref(context);
        if (pref == null) {
            return;
        }
        SharedPreferences.Editor edit = pref.edit();
        edit.putBoolean("pref_is_visible_tab_" + kind.name(), flag);
        edit.apply();
    }

    public static boolean toggleTabVisibility(Context context, NavigationKind kind) {
        boolean isVisible = PrefHelper.isVisibleTab(context, kind);
        PrefHelper.saveTabVisibility(context, kind, !isVisible);
        return !isVisible;
    }

    public static SharedPreferences getPref(Context context) {
        if (context == null) {
            return null;
        }
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }
}
