package com.example.coinkarasu.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

public class ResourceHelper {
    public static String[] getStringArrayResourceByName(Activity activity, String name) {
        Resources resources = activity.getResources();
        int resId = resources.getIdentifier(name, "array", activity.getPackageName());
        return resources.getStringArray(resId);
    }

    public static int getDrawableResourceIdByName(Activity activity, String name) {
        return activity.getResources().getIdentifier(name, "drawable", activity.getPackageName());
    }

    public static int getRawResId(Activity activity, String symbol) {
        String name = "ic_coin_" + symbol.toLowerCase();
        return activity.getResources().getIdentifier(name, "raw", activity.getPackageName());
    }
}
