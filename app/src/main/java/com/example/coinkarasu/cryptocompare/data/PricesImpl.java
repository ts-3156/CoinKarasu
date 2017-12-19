package com.example.coinkarasu.cryptocompare.data;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.coins.PriceMultiFullCoin;
import com.example.coinkarasu.coins.PriceMultiFullCoinImpl;
import com.example.coinkarasu.cryptocompare.response.PricesResponse;
import com.example.coinkarasu.cryptocompare.response.PricesResponseImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PricesImpl implements Prices {
    private PricesResponse response;
    private String exchange;

    private ArrayList<PriceMultiFullCoin> coins = new ArrayList<>();

    public PricesImpl() {
    }

    public PricesImpl(PricesResponse response) {
        this.response = response;
        this.exchange = null;
        extract(response);
    }

    public PricesImpl(PricesResponse response, String exchange) {
        this.response = response;
        this.exchange = exchange;
        extract(response);
    }

    private void extract(PricesResponse response) {
        JSONObject raw = response.getRaw();
        if (raw == null) {
            Log.e("extract", response.toString());
            return;
        }

        try {
            for (Iterator<String> it = raw.keys(); it.hasNext(); ) {
                String fromSymbol = it.next();
                JSONObject values = raw.getJSONObject(fromSymbol);

                String toSymbol = values.keys().next();
                JSONObject attrs = raw.getJSONObject(fromSymbol).getJSONObject(toSymbol);

                coins.add(new PriceMultiFullCoinImpl(attrs));
            }
        } catch (JSONException e) {
            Log.e("extract", e.getMessage());
        }
    }

    @Override
    public void merge(Prices prices) {
        coins.addAll(prices.getCoins());
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
    public void copyAttrsToCoin(Coin coin) {
        if (coin.isSectionHeader()) {
            return;
        }

        for (PriceMultiFullCoin c : coins) {
            if (coin.getSymbol().equals(c.getFromSymbol())) {
                coin.setPrice(c.getPrice());
                coin.setTrend(c.getChangePct24Hour() / 100.0);

                if (exchange != null) {
                    coin.setExchange(exchange);
                }

                break;
            }
        }
    }

    @Override
    public void copyAttrsToCoins(List<Coin> coins) {
        for (Coin coin : coins) {
            copyAttrsToCoin(coin);
        }
    }

    @Override
    public ArrayList<PriceMultiFullCoin> getCoins() {
        return coins;
    }

    @Override
    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    @Override
    public String getExchange() {
        return exchange;
    }
}
