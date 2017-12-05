package com.example.toolbartest.coins;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.toolbartest.cryptocompare.CoinListResponse;
import com.example.toolbartest.cryptocompare.CoinListResponseImpl;
import com.example.toolbartest.cryptocompare.Prices;
import com.example.toolbartest.tasks.FetchPricesTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class CoinListImpl implements CoinList {
    private CoinListResponse response;
    private HashMap<String, Double> prices;
    private HashMap<String, Double> trends;
    private String[] fromSymbols;
    private String toSymbol;

    public CoinListImpl(CoinListResponse response) {
        this.response = response;
        this.prices = null;
        this.trends = null;
        this.fromSymbols = null;
        this.toSymbol = null;
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

        if (coin != null) {
            setPriceAndTrend(coin);
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

        if (coin != null) {
            setPriceAndTrend(coin);
        }

        return coin;
    }

    private void setPriceAndTrend(Coin coin) {
        if (prices != null) {
            Double price = prices.get(coin.getSymbol());
            if (price != null) {
                coin.setPrice(price);
            }
        }

        if (trends != null) {
            Double trend = trends.get(coin.getSymbol());
            if (trend != null) {
                coin.setTrend(trend);
            }
        }
    }

    @Override
    public ArrayList<Coin> collectCoins() {
        final ArrayList<Coin> coins = new ArrayList<>(fromSymbols.length);

        for (String coinSymbol : fromSymbols) {
            Coin coin = getCoinBySymbol(coinSymbol);
            if (coin == null) {
                continue;
            }

            coin.setToSymbol(toSymbol);
            coins.add(coin);
        }

        return coins;
    }

    public boolean saveToFile(Context context) {
        return response != null && response.saveToFile(context);
    }

    @Override
    public void setPrices(HashMap<String, Double> prices) {
        this.prices = prices;
    }

    @Override
    public void setTrends(HashMap<String, Double> trends) {
        this.trends = trends;
    }

    @Override
    public void setFromSymbols(String[] fromSymbols) {
        this.fromSymbols = fromSymbols;
    }

    @Override
    public void setToSymbol(String toSymbol) {
        this.toSymbol = toSymbol;
    }

    @Override
    public void updatePrices(Activity activity, final UpdatePricesListener listener) {
        new FetchPricesTask(activity)
                .setFromSymbols(fromSymbols)
                .setToSymbol(toSymbol)
                .setListener(new FetchPricesTask.Listener() {
            @Override
            public void finished(JSONObject coinPricesResponse) {
                Prices prices = Prices.buildByResponse(coinPricesResponse);
                setPrices(prices.getPrices());
                setTrends(prices.getTrends());

                if (listener != null) {
                    listener.finished();
                }
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // @Override
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Activity activity;

        Builder() {
            this.activity = null;
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
    }

}
