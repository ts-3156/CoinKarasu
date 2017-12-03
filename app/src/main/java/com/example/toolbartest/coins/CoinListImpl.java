package com.example.toolbartest.coins;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.toolbartest.MainActivity;
import com.example.toolbartest.utils.SnackbarHelper;
import com.example.toolbartest.utils.VolleyHelper;

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
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
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

                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            VolleyError e = new VolleyError(new String(error.networkResponse.data));
                            SnackbarHelper.showSnackbar(activity, e.getMessage());
                        }

                    });

            request.setShouldCache(false);
            request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleyHelper.getInstance(activity.getApplicationContext()).addToRequestQueue(request);
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

    public interface Listener {
        void finished(CoinList coinList);
    }
}
