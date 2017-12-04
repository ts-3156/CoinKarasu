package com.example.toolbartest.coins;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.toolbartest.cryptocompare.CoinListResponse;
import com.example.toolbartest.cryptocompare.CoinListResponseImpl;
import com.example.toolbartest.cryptocompare.Request;
import com.example.toolbartest.utils.SnackbarHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class CoinListImpl implements CoinList {
    private CoinListResponse response;

    public CoinListImpl(CoinListResponse response) {
        this.response = response;
    }

    @Override
    public Coin getCoinBySymbol(String symbol) {
        if (response == null || response.getData() == null) {
            return null;
        }

        Coin coin = null;

        try {
            JSONObject attrs = response.getData().getJSONObject(symbol);
            coin = CoinImpl.buildByJSONObject(attrs);
        } catch (JSONException e) {
            Log.d("getCoinBySymbol", e.getMessage());
        }

        return coin;
    }

    @Override
    public Coin getCoinByCCId(String id) {
        if (response == null || response.getData() == null) {
            return null;
        }

        Coin coin = null;
        JSONObject data = response.getData();

        try {
            Iterator<String> keys = data.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject attrs = data.getJSONObject(key);
                if (attrs.getString("Id").equals(id)) {
                    coin = CoinImpl.buildByJSONObject(attrs);
                    break;
                }
            }
        } catch (JSONException e) {
            Log.d("getCoinByCCId", e.getMessage());
        }

        return coin;
    }

    @Override
    public String[] getDefaultCCWatchlistIds() {
        if (response == null || response.getData() == null) {
            return null;
        }

        String[] ids = {};

        try {
            ids = response.getResponse().getJSONObject("DefaultWatchlist").getString("CoinIs").split(",");
        } catch (JSONException e) {
            Log.d("getDefaultCCWatchlis...", e.getMessage());
        }

        return ids;
    }

    public boolean saveToFile(Context context) {
        return response != null && response.saveToFile(context);
    }

    // @Override
    public static Fetcher fetcher() {
        return new Fetcher();
    }

    // @Override
    public static Builder builder() {
        return new Builder();
    }

    public static class Fetcher {
        private static final String URL = "https://www.cryptocompare.com/api/data/coinlist/";

        private Activity activity;
        private Listener listener;

        Fetcher() {
            this.activity = null;
            this.listener = null;
        }

        public void fetch() {
            new Request(activity, URL).perform(new Request.Listener() {
                @Override
                public void finished(JSONObject response) {
                    CoinListResponse coinListResponse = new CoinListResponseImpl(response);

                    if (coinListResponse.isSuccess()) {
                        CoinList coinList = new CoinListImpl(coinListResponse);
                        if (listener != null) {
                            listener.finished(coinList);
                        }
                        coinList.saveToFile(activity);
                    } else {
                        SnackbarHelper.showSnackbar(activity, response.toString());
                    }
                }
            });
        }

        public Fetcher setActivity(Activity activity) {
            this.activity = activity;
            return this;
        }

        public Fetcher setListener(Listener listener) {
            this.listener = listener;
            return this;
        }
    }

    public static class Builder {
        private Activity activity;
        private String text;

        Builder() {
            this.activity = null;
            this.text = null;
        }

        public CoinList build() {
            CoinListResponse coinListResponse = CoinListResponseImpl.restoreFromFile(activity);
            if (coinListResponse == null || !coinListResponse.isSuccess()) {
                return null;
            }

            return new CoinListImpl(coinListResponse);
        }

        public Builder setActivity(Activity activity) {
            this.activity = activity;
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }
    }

}
