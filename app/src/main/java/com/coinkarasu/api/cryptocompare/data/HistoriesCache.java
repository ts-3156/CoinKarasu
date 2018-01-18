package com.coinkarasu.api.cryptocompare.data;

import android.content.Context;
import android.os.Looper;

import com.coinkarasu.api.cryptocompare.response.HistoryResponseImpl.HistoryKind;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.cache.StringArrayListCache;

import java.util.ArrayList;
import java.util.List;

import static com.coinkarasu.utils.cache.StringArrayListCache.makeCacheName;

/**
 * ClientImpl内から直接利用している。
 * HistoryKindに応じた有効期限がある。
 */
public final class HistoriesCache {
    private static final boolean DEBUG = true;
    private static final String TAG = "HistoriesCache";

    private StringArrayListCache<History> cache;

    public HistoriesCache(Context context) {
        cache = new StringArrayListCache<>(context.getCacheDir());
    }

    public synchronized void put(HistoryKind kind, String fromSymbol, String toSymbol, int limit, int aggregate, String exchange, List<History> histories) {
        String key = makeCacheName(TAG, kind, fromSymbol, toSymbol, limit, aggregate, exchange);

        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            new StringArrayListCache.WriteCacheToDiskTask<>(cache, key, histories).execute();
        } else {
            cache.put(key, histories);
        }
    }

    public synchronized List<History> get(HistoryKind kind, String fromSymbol, String toSymbol, int limit, int aggregate, String exchange) {
        String key = makeCacheName(TAG, kind, fromSymbol, toSymbol, limit, aggregate, exchange);
        List<String> list = cache.get(key, System.currentTimeMillis() - kind.expires);

        if (list == null || list.isEmpty()) {
            return null;
        }

        List<History> histories = new ArrayList<>(list.size());

        for (String str : list) {
            History history = HistoryImpl.buildByString(str);
            if (history == null) {
                if (DEBUG) CKLog.w(TAG, "get() History is null " + str);
                continue;
            }

            histories.add(history);
        }

        if (histories.isEmpty()) {
            cache.remove(key);
            return null;
        }

        return histories;
    }
}
