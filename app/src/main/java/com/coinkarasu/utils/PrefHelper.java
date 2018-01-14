package com.coinkarasu.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.R;
import com.coinkarasu.activities.etc.NavigationKind;

public class PrefHelper {
    private static final boolean DEBUG = true;
    private static final String TAG = "PrefHelper";

    public static final int DEFAULT_SYNC_INTERVAL = 60000;
    public static final int MIN_SYNC_INTERVAL = 30000;
    private static final int PREMIUM_MIN_SYNC_INTERVAL = 10000;

    private static boolean getAutoRefresh(Context context) {
        SharedPreferences pref = getPref(context);
        if (pref == null) {
            return false;
        }
        return pref.getBoolean("pref_auto_refresh", context.getResources().getBoolean(R.bool.auto_refresh));
    }

    public static int getSyncInterval(Context context) {
        boolean isAutoRefreshEnabled = getAutoRefresh(context);
        if (!isAutoRefreshEnabled) {
            return -1;
        }
        SharedPreferences pref = getPref(context);
        if (pref == null) {
            return DEFAULT_SYNC_INTERVAL;
        }

        String value = pref.getString("pref_sync_frequency", String.valueOf(DEFAULT_SYNC_INTERVAL));
        int interval = Integer.valueOf(value);

        int min = isPremium(context) ? PREMIUM_MIN_SYNC_INTERVAL : MIN_SYNC_INTERVAL;
        if (interval < min) {
            if (DEBUG) CKLog.d(TAG, "getSyncInterval() Invalid value " + interval);
            interval = min;
        }

        return interval;
    }

    public static int setDefaultSyncInterval(Context context) {
        SharedPreferences pref = getPref(context);
        if (pref == null) {
            return DEFAULT_SYNC_INTERVAL;
        }
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("pref_sync_frequency", String.valueOf(DEFAULT_SYNC_INTERVAL));
        edit.commit();
        return DEFAULT_SYNC_INTERVAL;
    }

    public static boolean isAnimEnabled(Context context) {
        return isEnabled(context, "pref_enable_price_anim", R.bool.enable_price_anim);
    }

    public static boolean isDownloadIconEnabled(Context context) {
        return isEnabled(context, "pref_enable_download_icon", R.bool.enable_download_icon);
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

    public static String getCkHost(Context context, String defaultValue) {
        if (!BuildConfig.DEBUG) {
            throw new UnsupportedOperationException();
        }
        SharedPreferences pref = getPref(context);
        if (pref == null) {
            return null;
        }
        return pref.getString("pref_change_ck_host", defaultValue);
    }

    public static boolean isVisibleTab(Context context, NavigationKind kind) {
        SharedPreferences pref = getPref(context);
        if (pref == null) {
            return false;
        }
        return pref.getBoolean("pref_is_visible_tab_" + kind.name(), kind.defaultVisibility);
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
        edit.commit();
    }

    public static boolean toggleTabVisibility(Context context, NavigationKind kind) {
        boolean isVisible = PrefHelper.isVisibleTab(context, kind);
        PrefHelper.saveTabVisibility(context, kind, !isVisible);
        return !isVisible;
    }

    public static void clear(Context context) {
        SharedPreferences pref = getPref(context);
        if (pref == null) {
            return;
        }
        SharedPreferences.Editor edit = pref.edit();
        edit.clear();
        edit.apply();
    }

    public static boolean isPremium(Context context) {
        return isEnabled(context, "pref_is_premium", R.bool.premium);
    }

    public static void setPremium(Context context, boolean flag) {
        SharedPreferences pref = getPref(context);
        if (pref == null) {
            return;
        }
        SharedPreferences.Editor edit = pref.edit();
        edit.putBoolean("pref_is_premium", flag);
        edit.apply();
    }

    public static boolean isDebugToastEnabled(Context context) {
        return BuildConfig.DEBUG && isEnabled(context, "pref_make_toast", R.bool.make_toast);
    }

    public static String getDebugToastLevel(Context context) {
        if (!BuildConfig.DEBUG) {
            throw new UnsupportedOperationException();
        }
        SharedPreferences pref = getPref(context);
        if (pref == null) {
            return null;
        }
        return pref.getString("pref_toast_level", "debug");
    }

    public static boolean isKeepScreenOnEnabled(Context context) {
        return BuildConfig.DEBUG && isEnabled(context, "pref_keep_screen_on", R.bool.keep_screen_on);
    }

    public static boolean isDebugPremium(Context context) {
        return BuildConfig.DEBUG && isEnabled(context, "pref_become_premium", R.bool.become_premium);
    }

    private static boolean isEnabled(Context context, String key, int defResId) {
        SharedPreferences pref = getPref(context);
        return pref != null && pref.getBoolean(key, context.getResources().getBoolean(defResId));
    }

    public static SharedPreferences getPref(Context context) {
        if (context == null) {
            return null;
        }
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }
}
