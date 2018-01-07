package com.coinkarasu.tasks.by_exchange.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.utils.DiskCacheHelper2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PricesCache {

    private static final boolean DEBUG = true;
    private static final String TAG = "PricesCache";

    private Context context;

    public PricesCache(Context context) {
        this.context = context;
    }

    public synchronized void put(NavigationKind kind, Exchange exchange, CoinKind coinKind, ArrayList<Price> prices) {
        if (prices == null || prices.isEmpty()) {
            return;
        }
        new WriteCacheToDiskTask(getFileFor(kind, exchange, coinKind)).execute(prices);
    }

    public synchronized List<Price> get(NavigationKind kind, Exchange exchange, CoinKind coinKind) {
        String text = DiskCacheHelper2.read(getFileFor(kind, exchange, coinKind));
        if (text == null) {
            return null;
        }

        ArrayList<Price> prices = new ArrayList<>();

        try {
            JSONArray json = new JSONArray(text);
            for (int i = 0; i < json.length(); i++) {
                JSONObject attrs = json.getJSONObject(i);
                prices.add(Price.buildByJson(attrs));
            }
        } catch (JSONException e) {
            if (DEBUG) Log.e(TAG, e.getMessage());
            prices = null;
        }

        if (prices == null || prices.isEmpty()) {
            remove(kind, exchange, coinKind);
        }

        return prices;
    }

    private synchronized void remove(NavigationKind kind, Exchange exchange, CoinKind coinKind) {
        if (!getFileFor(kind, exchange, coinKind).delete()) {
            Log.d(TAG, "Could not delete cache file for "
                    + kind.name() + ", " + exchange.name() + ", " + coinKind.name());
        }
    }

    private static String makeCacheName(NavigationKind kind, Exchange exchange, CoinKind coinKind) {
        return "cached_prices_" + kind.name() + "_" + exchange.name() + "_" + coinKind.name() + ".json";
    }

    private File getFileFor(NavigationKind kind, Exchange exchange, CoinKind coinKind) {
        return new File(context.getCacheDir(), makeCacheName(kind, exchange, coinKind));
    }


    private static class WriteCacheToDiskTask extends AsyncTask<ArrayList<Price>, Void, Void> {
        private File file;

        WriteCacheToDiskTask(File file) {
            this.file = file;
        }

        @Override
        protected Void doInBackground(ArrayList<Price>... params) {
            JSONArray json = new JSONArray();
            for (Price price : params[0]) {
                json.put(price.toJson());
            }
            DiskCacheHelper2.write(file, json.toString());
            return null;
        }
    }
}
