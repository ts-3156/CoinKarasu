package com.coinkarasu.utils.io;

import android.content.Context;

import com.coinkarasu.utils.CKLog;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class CacheFileHelper {

    private static final boolean DEBUG = true;
    private static final String TAG = "CacheFileHelper";

    public static void write(Context context, String name, String text) {
        FileHelper.write(new File(context.getCacheDir(), name), text);
    }

    public static String read(Context context, String name) {
        return FileHelper.read(new File(context.getCacheDir(), name));
    }

    public static boolean exists(Context context, String name) {
        File file = new File(context.getCacheDir(), name);
        return file.exists();
    }

    public static Date lastModified(Context context, String name) {
        File file = new File(context.getCacheDir(), name);
        return new Date(file.lastModified());
    }

    public static boolean isExpired(Context context, String name, long duration) {
        return new Date(System.currentTimeMillis() - duration).compareTo(lastModified(context, name)) > 0;
    }

    public static boolean touch(Context context, String name) {
        File file = new File(context.getCacheDir(), name);
        try {
            file.createNewFile();
        } catch (IOException e) {
            CKLog.e(TAG, e);
        }
        return file.setLastModified(System.currentTimeMillis());
    }
}
