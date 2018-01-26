package com.coinkarasu.chart;

import android.content.Context;
import android.os.Looper;

import com.coinkarasu.activities.etc.PieChartKind;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.cache.StringArrayListCache;
import com.coinkarasu.utils.cache.StringArrayListCache.WriteCacheToDiskTask;

import java.util.ArrayList;
import java.util.List;

import static com.coinkarasu.utils.cache.StringArrayListCache.makeCacheName;

/**
 * CoinPieChartTabContentFragmentで、最後のデータを表示するために利用している。
 * 有効期限はなく、常に上書きする。
 */
public final class EntriesCache {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "EntriesCache";

    private StringArrayListCache<Entry> cache;

    public EntriesCache(Context context) {
        cache = new StringArrayListCache<>(context.getCacheDir());
    }

    public synchronized void put(PieChartKind kind, String fromSymbol, String toSymbol, List<Entry> entries) {
        String key = makeCacheName(TAG, kind, fromSymbol, toSymbol);

        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            new WriteCacheToDiskTask<>(cache, key, entries).execute();
        } else {
            cache.put(key, entries);
        }
    }

    public synchronized List<Entry> get(PieChartKind kind, String fromSymbol, String toSymbol) {
        String key = makeCacheName(TAG, kind, fromSymbol, toSymbol);
        List<String> list = cache.get(key);

        if (list == null || list.isEmpty()) {
            return null;
        }

        List<Entry> entries = new ArrayList<>(list.size());

        for (String str : list) {
            Entry entry = Entry.buildBy(str);
            if (entry == null) {
                if (DEBUG) CKLog.w(TAG, "get() Entry is null " + str);
                continue;
            }

            entries.add(entry);
        }

        if (entries.isEmpty()) {
            cache.remove(key);
            return null;
        }

        return entries;
    }
}
