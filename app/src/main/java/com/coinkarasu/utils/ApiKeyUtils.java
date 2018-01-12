package com.coinkarasu.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.utils.io.FileHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class ApiKeyUtils {
    private static final boolean DEBUG = true;
    private static final String TAG = "ApiKeyUtils";
    private static final String NAME = "token";

    public static Pair<String, String> get(Context context) {
        String text = FileHelper.read(new File(context.getFilesDir(), NAME));
        String key = null, secret = null;
        try {
            JSONObject json = new JSONObject(text);
            key = json.getString("key");
            secret = json.getString("secret");
        } catch (JSONException e) {
            CKLog.e(TAG, e);
        }
        return Pair.create(key, secret);
    }

    public static Pair<String, String> dummy() {
        return Pair.create(BuildConfig.CK_TMP_KEY, BuildConfig.CK_TMP_SECRET);
    }

    public static void save(Context context, String key, String secret) {
        JSONObject json = new JSONObject();
        try {
            json.put("key", key);
            json.put("secret", secret);
        } catch (JSONException e) {
            CKLog.e(TAG, e);
        }
        FileHelper.write(new File(context.getFilesDir(), NAME), json.toString());
    }

    public static boolean exists(Context context) {
        File file = new File(context.getFilesDir(), NAME);
        if (!file.exists()) {
            return false;
        }

        String text = FileHelper.read(file);
        if (TextUtils.isEmpty(text)) {
            return false;
        }

        String key = null, secret = null;
        try {
            JSONObject json = new JSONObject(text);
            key = json.getString("key");
            secret = json.getString("secret");
        } catch (JSONException e) {
            CKLog.e(TAG, e);
        }

        return !TextUtils.isEmpty(key) && !TextUtils.isEmpty(secret);
    }
}
