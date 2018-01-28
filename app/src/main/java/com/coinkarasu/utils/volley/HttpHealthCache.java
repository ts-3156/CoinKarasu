package com.coinkarasu.utils.volley;

import android.support.v4.util.LruCache;

import com.coinkarasu.utils.CKDateUtils;
import com.coinkarasu.utils.CKLog;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpHealthCache {
    private static final boolean DEBUG = false;
    private static final String TAG = "HttpHealthCache";
    private static final long EXPIRATION = TimeUnit.MINUTES.toMillis(5);
    private static final int MAX_SIZE = 3;

    private LruCache<Long, Boolean> cache;
    private String key;

    public HttpHealthCache(String key) {
        this.key = key;
        cache = new LruCache<>(MAX_SIZE);
    }

    public void put(boolean isSuccess) {
        cache.put(CKDateUtils.now(), isSuccess);
    }

    public boolean isOperatingNormally() {
        Map<Long, Boolean> snapshot = cache.snapshot();
        long expiration = CKDateUtils.now() - EXPIRATION;
        int error = 0;

        for (Map.Entry<Long, Boolean> entry : snapshot.entrySet()) {
            if (expiration < entry.getKey() && !entry.getValue()) {
                error++;
            }
        }

        if (DEBUG) CKLog.d(TAG, "isOperatingNormally() " + key + " "
                + (error < MAX_SIZE) + " " + error + "/" + MAX_SIZE);

        return error < MAX_SIZE;
    }
}
