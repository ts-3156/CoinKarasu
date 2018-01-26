package com.coinkarasu.api.cryptocompare.data;

import android.content.Context;
import android.os.Looper;

import com.coinkarasu.api.cryptocompare.response.HistoryResponseImpl.HistoryKind;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.cache.StringArrayListCache;

import java.util.ArrayList;
import java.util.List;

import static com.coinkarasu.utils.cache.StringArrayListCache.makeCacheName;

public final class HistoriesCache {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "HistoriesCache";

    private StringArrayListCache<History> cache;

    public HistoriesCache(Context context) {
        cache = new StringArrayListCache<>(context.getCacheDir());
    }

    /**
     * Clientの中からキャッシュを保存する時に使っている。細かい粒度でキャッシュを分けるために引数が多い
     */
    public synchronized void put(HistoryKind kind, String fromSymbol, String toSymbol, int limit, int aggregate, String exchange, List<History> histories) {
        put(makeCacheName(kind, fromSymbol, toSymbol, limit, aggregate, exchange), histories);
    }

    /**
     * Fragmentから利用している。大きな粒度でキャッシュを分けるためにtagだけしかキーの引数がない
     */
    public synchronized void put(String tag, List<History> histories) {
        String key = TAG + tag;

        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            new StringArrayListCache.WriteCacheToDiskTask<>(cache, key, histories).execute();
        } else {
            cache.put(key, histories);
        }
    }

    /**
     * 有効期限と関係なく、ファイルが存在すればそのキャッシュを返す
     */
    public synchronized List<History> get(HistoryKind kind, String fromSymbol, String toSymbol, int limit, int aggregate, String exchange) {
        return get(kind, fromSymbol, toSymbol, limit, aggregate, exchange, false);
    }

    /**
     * 有効期限を使うかどうか指定するフラグがある
     */
    public synchronized List<History> get(HistoryKind kind, String fromSymbol, String toSymbol, int limit, int aggregate, String exchange, boolean ignoreExpires) {
        return get(makeCacheName(kind, fromSymbol, toSymbol, limit, aggregate, exchange), ignoreExpires, kind.expires);
    }

    /**
     * Fragmentから利用している。大きな粒度でキャッシュを分けるためにtagだけしかキーの引数がない
     */
    public synchronized List<History> get(String tag) {
        return get(tag, true, -1);
    }

    private List<History> get(String tag, boolean ignoreExpires, long expires) {
        String key = TAG + tag;
        List<String> list;

        if (ignoreExpires) {
            list = cache.get(key);
        } else {
            list = cache.get(key, System.currentTimeMillis() - expires);
        }

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

    public boolean exists(HistoryKind kind, String fromSymbol, String toSymbol, int limit, int aggregate, String exchange) {
        return cache.exists(makeCacheName(TAG, kind, fromSymbol, toSymbol, limit, aggregate, exchange));
    }

    public boolean isExpired(HistoryKind kind, String fromSymbol, String toSymbol, int limit, int aggregate, String exchange) {
        return cache.isExpired(makeCacheName(TAG, kind, fromSymbol, toSymbol, limit, aggregate, exchange), System.currentTimeMillis() - kind.expires);
    }
}
