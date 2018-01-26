package com.coinkarasu.utils;

import android.content.Context;
import android.text.TextUtils;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.utils.io.FileHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class ApiKeyUtils {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "ApiKeyUtils";
    private static final String NAME = "token";

    public static Token get(Context context) {
        String text = FileHelper.read(new File(context.getFilesDir(), NAME));
        String key = null, secret = null;
        try {
            JSONObject json = new JSONObject(text);
            key = json.getString("key");
            secret = json.getString("secret");
        } catch (JSONException e) {
            CKLog.e(TAG, e);
        }
        return new Token(key, secret);
    }

    public static Token dummy() {
        return new Token(BuildConfig.CK_TMP_KEY, BuildConfig.CK_TMP_SECRET);
    }

    public static void save(Context context, Token token) {
        if (DEBUG) CKLog.d(TAG, "save() " + token.toString());
        JSONObject json = new JSONObject();
        try {
            json.put("key", token.getKey());
            json.put("secret", token.getSecret());
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

    public static void remove(Context context) {
        File file = new File(context.getFilesDir(), NAME);
        file.delete();
    }
}
