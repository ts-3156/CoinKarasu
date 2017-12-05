package com.example.toolbartest.cryptocompare.data;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.coins.CoinImpl;
import com.example.toolbartest.cryptocompare.response.CoinListResponse;
import com.example.toolbartest.cryptocompare.response.CoinListResponseImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class CoinListImpl implements CoinList {
    private CoinListResponse response;
    private HashMap<String, Double> prices;
    private HashMap<String, Double> trends;
    private Prices pricesObj;

    public CoinListImpl(CoinListResponse response) {
        this.response = response;
        this.prices = null;
        this.trends = null;
        this.pricesObj = null;
    }

    // @Override
    public static CoinList buildByResponse(JSONObject response) {
        return new CoinListImpl(new CoinListResponseImpl(response));
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
    public ArrayList<Coin> collectCoins(String[] fromSymbols, String toSymbol) {
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

    @Override
    public boolean saveToCache(Context context) {
        return response != null && response.saveToCache(context);
    }

    // @Override
    public static CoinList restoreFromCache(Activity activity) {
        CoinListResponse coinListResponse = CoinListResponseImpl.restoreFromCache(activity);
        if (coinListResponse == null || !coinListResponse.isSuccess()) {
            return null;
        }

        return new CoinListImpl(coinListResponse);
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
    public void setPrices(Prices prices) {
        this.pricesObj = prices;
        setPrices(prices.getPrices());
        setTrends(prices.getTrends());
    }
}
