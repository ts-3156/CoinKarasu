package com.coinkarasu.tasks.by_exchange.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.utils.cache.StringArrayListCache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PricesCache {

    private static final boolean DEBUG = true;
    private static final String TAG = "PricesCache";

    private StringArrayListCache cache;

    public PricesCache(Context context) {
        cache = new StringArrayListCache(context.getCacheDir());
    }

    public synchronized void put(NavigationKind kind, Exchange exchange, CoinKind coinKind, ArrayList<Price> prices) {
        if (prices == null || prices.isEmpty()) {
            return;
        }

        String[] list = new String[prices.size()];
        for (int i = 0; i < prices.size(); i++) {
            list[i] = prices.get(i).toString();
        }

        new WriteCacheToDiskTask(cache, makeCacheName(kind, exchange, coinKind)).execute(list);
    }

    public synchronized List<Price> get(NavigationKind kind, Exchange exchange, CoinKind coinKind) {
        List<String> list = cache.get(makeCacheName(kind, exchange, coinKind));
        if (list == null || list.isEmpty()) {
            remove(kind, exchange, coinKind);
            return null;
        }

        List<Price> prices = new ArrayList<>(list.size());

        for (String str : list) {
            Price price = Price.buildByString(str);
            if (price == null) {
                if (DEBUG) Log.e(TAG, "Price is null " + str);
                continue;
            }

            prices.add(price);
        }

        if (prices.isEmpty()) {
            remove(kind, exchange, coinKind);
            return null;
        }

        return prices;
    }

    private synchronized void remove(NavigationKind kind, Exchange exchange, CoinKind coinKind) {
        cache.remove(makeCacheName(kind, exchange, coinKind));
    }

    private static String makeCacheName(NavigationKind kind, Exchange exchange, CoinKind coinKind) {
        return "cached_prices_" + kind.name() + "_" + exchange.name() + "_" + coinKind.name() + ".json";
    }

    private static class WriteCacheToDiskTask extends AsyncTask<String, Void, Void> {
        private StringArrayListCache cache;
        private String key;

        WriteCacheToDiskTask(StringArrayListCache cache, String key) {
            this.cache = cache;
            this.key = key;
        }

        @Override
        protected Void doInBackground(String... params) {
            cache.put(key, Arrays.asList(params));
            return null;
        }
    }
}
