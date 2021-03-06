package com.coinkarasu.utils;

import android.content.Context;
import android.text.TextUtils;

import com.coinkarasu.utils.io.FileHelper;

import java.io.File;
import java.util.UUID;

public class UuidUtils {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "UuidUtils";
    private static final String NAME = "uuid";

    public static String get(Context context) {
        return FileHelper.read(new File(context.getFilesDir(), NAME));
    }

    public static synchronized String getOrGenerateIfBlank(Context context) {
        File file = new File(context.getFilesDir(), NAME);
        if (!file.exists()) {
            FileHelper.touch(file);
        }

        String uuid = FileHelper.read(file);
        if (TextUtils.isEmpty(uuid)) {
            uuid = UUID.randomUUID().toString();
            FileHelper.write(file, uuid);
        }

        return uuid;
    }

    public static boolean exists(Context context) {
        File file = new File(context.getFilesDir(), NAME);
        return file.exists() && !TextUtils.isEmpty(FileHelper.read(file));

    }

    public static void remove(Context context) {
        File file = new File(context.getFilesDir(), NAME);
        file.delete();

    }
}
