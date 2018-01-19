package com.coinkarasu.utils.cache;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.CKStringUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StringArrayListCache<T> {
    private static final boolean DEBUG = true;
    private static final String TAG = "ArrayListCache";

    private DiskBasedCache cache;

    public StringArrayListCache(File rootDir) {
        cache = new DiskBasedCache(rootDir);
    }

    public void put(String key, List<T> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        JSONArray json = new JSONArray();
        for (T t : list) {
            String str = t.toString();
            if (TextUtils.isEmpty(str)) {
                if (DEBUG) CKLog.w(TAG, "put() str is empty key=" + key);
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
                    if (DEBUG) CKLog.w(TAG, "get() str is empty index=" + i + " key=" + key);
                    continue;
                }

                list.add(str);
            }
        } catch (JSONException e) {
            CKLog.e(TAG, e);
            list = null;
        }

        if (list == null || list.isEmpty()) {
            cache.remove(key);
            return null;
        }

        return list;
    }

    public boolean exists(String key) {
        return cache.exists(key);
    }

    public boolean isExpired(String key, long expiration) {
        return cache.isExpired(key, expiration);
    }

    public void remove(String key) {
        cache.remove(key);
    }

    public static String makeCacheName(Object... params) {
        return CKStringUtils.join("_", params);
    }

    public static class WriteCacheToDiskTask<T> extends AsyncTask<Void, Void, Void> {
        private StringArrayListCache<T> cache;
        private String key;
        private List<T> list;

        public WriteCacheToDiskTask(StringArrayListCache<T> cache, String key, List<T> list) {
            this.cache = cache;
            this.key = key;
            this.list = list;
        }

        @Override
        protected Void doInBackground(Void... params) {
            cache.put(key, list);
            cache = null;
            key = null;
            list = null;
            return null;
        }
    }
}
