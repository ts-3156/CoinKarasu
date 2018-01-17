package com.coinkarasu.utils.cache;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StringArrayListCache {

    private static final boolean DEBUG = true;
    private static final String TAG = "ArrayListCache";

    private DiskBasedCache cache;

    public StringArrayListCache(File rootDir) {
        cache = new DiskBasedCache(rootDir);
    }

    public void put(String key, List<String> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        JSONArray json = new JSONArray();
        for (String str : list) {
            if (TextUtils.isEmpty(str)) {
                if (DEBUG) Log.e(TAG, "put() str is empty key=" + key);
                continue;
            }

            json.put(str);
        }

        cache.put(key, new Cache.Entry(json.toString()));
    }

    public List<String> get(String key, long expiration) {
        if (cache.exists(key) && !cache.isExpired(key, expiration)) {
            return get(key);
        } else {
            return null;
        }
    }

    public List<String> get(String key) {
        if (!cache.exists(key)) {
            return null;
        }

        Cache.Entry entry = cache.get(key);
        if (entry == null || TextUtils.isEmpty(entry.data)) {
            cache.remove(key);
            return null;
        }

        List<String> list = new ArrayList<>();

        try {
            JSONArray json = new JSONArray(entry.data);
            for (int i = 0; i < json.length(); i++) {
                String str = json.getString(i);
                if (TextUtils.isEmpty(str)) {
                    if (DEBUG) Log.e(TAG, "get() str is empty index=" + i + " key=" + key);
                    continue;
                }

                list.add(str);
            }
        } catch (JSONException e) {
            if (DEBUG) Log.e(TAG, e.getMessage());
            list = null;
        }

        if (list == null || list.isEmpty()) {
            cache.remove(key);
            return null;
        }

        return list;
    }

    public void remove(String key) {
        cache.remove(key);
    }
}
