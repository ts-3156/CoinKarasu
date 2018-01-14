package com.coinkarasu.utils.volley;

import android.support.v4.util.LruCache;

import com.coinkarasu.utils.CKLog;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpResultCache {
    private static final boolean DEBUG = false;
    private static final String TAG = "HttpResultCache";
    private static final long EXPIRATION = TimeUnit.MINUTES.toMillis(5);
    private static final int MAX_SIZE = 3;

    private LruCache<Long, Boolean> cache;
    private String key;

    public HttpResultCache(String key) {
        this.key = key;
        cache = new LruCache<>(MAX_SIZE);
    }

    public void put(boolean isSuccess) {
        cache.put(System.currentTimeMillis(), isSuccess);
    }

    public boolean areAllFailure() {
        Map<Long, Boolean> snapshot = cache.snapshot();
        long expiration = System.currentTimeMillis() - EXPIRATION;
        int count = 0;

        for (Map.Entry<Long, Boolean> entry : snapshot.entrySet()) {
            if (expiration < entry.getKey() && !entry.getValue()) {
                count++;
            }
        }

        if (DEBUG) CKLog.d(TAG, "areAllFailure() " + key + " "
                + (count == MAX_SIZE) + " " + count + "/" + MAX_SIZE);

        return count == MAX_SIZE;
    }
}
