package com.coinkarasu.utils;

import android.content.Context;

import com.coinkarasu.utils.io.CacheFileHelper;

public class IntentServiceIntervalChecker {
    public static boolean shouldRun(Context context, String tag, long expiration) {
        return !CacheFileHelper.exists(context, tag) || CacheFileHelper.isExpired(context, tag, expiration);
    }

    public static void onStart(Context context, String tag) {
        CacheFileHelper.touch(context, tag);
    }
}
