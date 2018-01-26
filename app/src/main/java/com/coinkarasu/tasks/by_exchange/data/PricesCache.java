package com.coinkarasu.tasks.by_exchange.data;

import android.content.Context;
import android.os.Looper;

import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.cache.StringArrayListCache;
import com.coinkarasu.utils.cache.StringArrayListCache.WriteCacheToDiskTask;

import java.util.ArrayList;
import java.util.List;

import static com.coinkarasu.utils.cache.StringArrayListCache.makeCacheName;

/**
 * CoinListSectionFragmentで、最後のデータを表示するために利用している。
 * 有効期限はなく、常に上書きする。
 */
public final class PricesCache {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "PricesCache";

    private StringArrayListCache<Price> cache;

    public PricesCache(Context context) {
        cache = new StringArrayListCache<>(context.getCacheDir());
    }

    public synchronized void put(NavigationKind kind, Exchange exchange, CoinKind coinKind, List<Price> prices) {
        String key = makeCacheName(TAG, kind, exchange, coinKind);

        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            new WriteCacheToDiskTask<>(cache, key, prices).execute();
        } else {
            cache.put(key, prices);
        }
    }

    public synchronized List<Price> get(NavigationKind kind, Exchange exchange, CoinKind coinKind) {
        String key = makeCacheName(TAG, kind, exchange, coinKind);
        List<String> list = cache.get(key);

        if (list == null || list.isEmpty()) {
            return null;
        }

        List<Price> prices = new ArrayList<>(list.size());

        for (String str : list) {
            Price price = Price.buildByString(str);
            if (price == null) {
                if (DEBUG) CKLog.w(TAG, "get() Price is null " + str);
                continue;
            }

            prices.add(price);
        }

        if (prices.isEmpty()) {
            cache.remove(key);
            return null;
        }

        return prices;
    }
}
