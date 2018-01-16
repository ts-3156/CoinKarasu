package com.coinkarasu.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

// android.support.v4.app.AppLaunchChecker
public class AppTutorialChecker {
    private static final String KEY = "tutorialFinished";

    public static boolean hasStarted(@NonNull Context context, String id) {
        return PrefHelper.getPref(context).getBoolean(makeKey(id), false);
    }

    public static void onTutorialFinished(@NonNull Activity activity, String id) {
        final SharedPreferences sp = PrefHelper.getPref(activity);
        if (sp.getBoolean(makeKey(id), false)) {
            return;
        }

        sp.edit().putBoolean(makeKey(id), true).apply();
    }

    private static String makeKey(String id) {
        return KEY + id;
    }
}
