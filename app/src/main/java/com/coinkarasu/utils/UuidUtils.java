package com.coinkarasu.utils;

import android.content.Context;
import android.text.TextUtils;

import com.coinkarasu.utils.io.FileHelper;

import java.io.File;
import java.util.UUID;

public class UuidUtils {
    private static final boolean DEBUG = true;
    private static final String TAG = "UuidUtils";
    private static final String NAME = "uuid";

    public static String get(Context context) {
        return FileHelper.read(new File(context.getFilesDir(), NAME));
    }

    public static String getOrGenerateIfBlank(Context context) {
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
        if (!file.exists()) {
            return false;
        }

        return !TextUtils.isEmpty(FileHelper.read(file));
    }
}
