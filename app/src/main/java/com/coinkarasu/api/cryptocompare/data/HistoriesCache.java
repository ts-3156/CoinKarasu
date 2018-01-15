package com.coinkarasu.api.cryptocompare.data;

import android.content.Context;
import android.os.Looper;

import com.coinkarasu.api.cryptocompare.response.HistoryResponseImpl.HistoryKind;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.cache.StringArrayListCache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ClientImpl内から直接利用している。
 * HistoryKindに応じた有効期限がある。
 */
public class HistoriesCache {

    private static final boolean DEBUG = true;
    private static final String TAG = "HistoriesCache";

    private StringArrayListCache cache;

    public HistoriesCache(Context context) {
        cache = new StringArrayListCache(context.getCacheDir());
    }

    public synchronized void put(HistoryKind kind, String fromSymbol, String toSymbol, int limit, int aggregate, String exchange, List<History> histories) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            throw new RuntimeException("Shouldn't call put() from the main thread");
        }

        if (histories == null || histories.isEmpty()) {
            return;
        }

        String[] list = new String[histories.size()];
        for (int i = 0; i < histories.size(); i++) {
            list[i] = histories.get(i).toString();
        }

        cache.put(makeCacheName(kind, fromSymbol, toSymbol, limit, aggregate, exchange), Arrays.asList(list));
    }

    public synchronized List<History> get(HistoryKind kind, String fromSymbol, String toSymbol, int limit, int aggregate, String exchange) {
        List<String> list = cache.get(makeCacheName(kind, fromSymbol, toSymbol, limit, aggregate, exchange), System.currentTimeMillis() - kind.expires);
        if (list == null || list.isEmpty()) {
            remove(kind, fromSymbol, toSymbol, limit, aggregate, exchange);
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
            remove(kind, fromSymbol, toSymbol, limit, aggregate, exchange);
            return null;
        }

        return histories;
    }

    private synchronized void remove(Object... params) {
        cache.remove(makeCacheName(params));
    }

    private String makeCacheName(Object... params) {
        StringBuilder builder = new StringBuilder();
        String delim = "_";

        for (Object param : params) {
            builder.append(delim);
            builder.append(param.toString());
        }

        return "cached_histories" + builder.toString() + ".json";
    }
}
