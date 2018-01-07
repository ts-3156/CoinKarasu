package com.coinkarasu.tasks.by_exchange.data;

import android.content.Context;
import android.util.Log;

import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.utils.DiskCacheHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CachedPrices {

    private static final boolean DEBUG = true;
    private static final String TAG = "CachedPrices";

    private ArrayList<Price> prices;
    private boolean isCache;

    public CachedPrices(ArrayList<Price> prices) {
        this(prices, false);
    }

    public CachedPrices(ArrayList<Price> prices, boolean isCache) {
        this.prices = prices;
        this.isCache = isCache;
    }

    public List<Price> getPrices() {
        return prices;
    }

    private static String getCacheName(NavigationKind kind, Exchange exchange, CoinKind coinKind) {
        return "cached_prices_" + kind.name() + "_" + exchange.name() + "_" + coinKind.name() + ".json";
    }

    public boolean saveToCache(Context context, NavigationKind kind, Exchange exchange, CoinKind coinKind) {
        JSONArray array = new JSONArray();
        for (Price price : prices) {
            array.put(price.toJson());
        }

        DiskCacheHelper.write(context, getCacheName(kind, exchange, coinKind), array.toString());
        return true;
    }

    public boolean isCache() {
        return isCache;
    }

    public static CachedPrices restoreFromCache(Context context, NavigationKind kind, Exchange exchange, CoinKind coinKind) {
        String text = DiskCacheHelper.read(context, getCacheName(kind, exchange, coinKind));
        ArrayList<Price> prices = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(text);
            for (int i = 0; i < array.length(); i++) {
                JSONObject attrs = array.getJSONObject(i);
                prices.add(Price.buildByJson(attrs));
            }
        } catch (JSONException e) {
            if (DEBUG) Log.e(TAG, e.getMessage());
            prices = null;
        }

        return new CachedPrices(prices, true);
    }

    public static boolean isCacheExist(Context context, NavigationKind kind, Exchange exchange, CoinKind coinKind) {
        return DiskCacheHelper.exists(context, getCacheName(kind, exchange, coinKind));
    }

}
