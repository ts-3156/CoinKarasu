package com.example.toolbartest.cryptocompare.data;

import android.app.Activity;
import android.content.Context;

import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.cryptocompare.response.PricesResponse;
import com.example.toolbartest.cryptocompare.response.PricesResponseImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PricesImpl implements Prices {
    private PricesResponse response;
    private String exchange;

    private HashMap<String, Double> prices;
    private HashMap<String, Double> trends;

    public PricesImpl(PricesResponse response) {
        this.response = response;
        this.exchange = null;
        extract();
    }

    public PricesImpl(PricesResponse response, String exchange) {
        this.response = response;
        this.exchange = exchange;
        extract();
    }

    private void extract() {
        JSONObject raw = response.getRaw();
        if (raw == null) {
            return;
        }

        prices = new HashMap<>();
        trends = new HashMap<>();

        try {
            for (Iterator<String> it = raw.keys(); it.hasNext(); ) {
                String fromSymbol = it.next();
                JSONObject values = raw.getJSONObject(fromSymbol);

                String toSymbol = values.keys().next();
                JSONObject attrs = raw.getJSONObject(fromSymbol).getJSONObject(toSymbol);

                prices.put(fromSymbol, attrs.getDouble("PRICE"));
                trends.put(fromSymbol, attrs.getDouble("CHANGEPCT24HOUR") / 100.0);
            }
        } catch (JSONException e) {
        }
    }

    public static PricesImpl buildByResponse(JSONObject response) {
        return new PricesImpl(new PricesResponseImpl(response));
    }

    @Override
    public boolean saveToCache(Context context) {
        return response != null && response.saveToCache(context);
    }

    // @Override
    public static Prices restoreFromCache(Activity activity) {
        PricesResponse pricesResponse = PricesResponseImpl.restoreFromCache(activity);
        if (pricesResponse == null) {
            return null;
        }

        return new PricesImpl(pricesResponse);
    }

    private void restorePricesFromCache() {

    }

    @Override
    public void setAttrsToCoin(Coin coin) {
        Double price = prices.get(coin.getSymbol());
        if (price != null) {
            coin.setPrice(price);
        }

        Double trend = trends.get(coin.getSymbol());
        if (trend != null) {
            coin.setTrend(trend);
        }

        if (exchange != null) {
            coin.setExchange(exchange);
        }
    }

    @Override
    public void setAttrsToCoins(List<Coin> coins) {
        for(Coin coin: coins) {
            setAttrsToCoin(coin);
        }
    }

    @Override
    public String getExchange() {
        return exchange;
    }
}
