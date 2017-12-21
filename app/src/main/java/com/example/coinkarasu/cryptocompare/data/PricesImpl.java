package com.example.coinkarasu.cryptocompare.data;

import android.content.Context;
import android.util.Log;

import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.coins.PriceMultiFullCoin;
import com.example.coinkarasu.coins.PriceMultiFullCoinImpl;
import com.example.coinkarasu.cryptocompare.response.PricesResponse;
import com.example.coinkarasu.utils.CacheHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PricesImpl implements Prices {
    private String exchange;
    private boolean isCache;

    private ArrayList<PriceMultiFullCoin> coins;

    public PricesImpl(String exchange) {
        this.exchange = exchange;
        this.coins = new ArrayList<>();
        this.isCache = false;
    }

    private PricesImpl(String exchange, ArrayList<PriceMultiFullCoin> coins, boolean isCache) {
        this.exchange = exchange;
        this.coins = coins;
        this.isCache = isCache;
    }

    public PricesImpl(PricesResponse response) {
        this.exchange = response.getExchange();
        this.coins = new ArrayList<>();
        this.isCache = false;
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

    private static String getCacheName(String tag) {
        return "prices_" + tag + ".json";
    }

    @Override
    public boolean saveToCache(Context context) {
        return saveToCache(context, "default_tag");
    }

    @Override
    public boolean saveToCache(Context context, String tag) {
        JSONArray array = new JSONArray();
        for (PriceMultiFullCoin coin : coins) {
            array.put(coin.toJson());
        }

        JSONObject data = new JSONObject();
        try {
            data.put("_exchange", exchange);
            data.put("_coins", array);
        } catch (JSONException e) {
            Log.e("saveToCache", e.getMessage());
        }

        CacheHelper.write(context, getCacheName(tag), data.toString());
        return true;
    }

    @Override
    public boolean isCache() {
        return isCache;
    }

    // @Override
    public static Prices restoreFromCache(Context context, String tag) {
        String text = CacheHelper.read(context, getCacheName(tag));
        JSONObject data;
        ArrayList<PriceMultiFullCoin> coins = new ArrayList<>();
        String exchange = null;

        try {
            data = new JSONObject(text);

            JSONArray array = data.getJSONArray("_coins");
            for (int i = 0; i < array.length(); i++) {
                JSONObject attrs = array.getJSONObject(i);
                coins.add(new PriceMultiFullCoinImpl(attrs));
            }
            exchange = data.getString("_exchange");
        } catch (JSONException e) {
            Log.e("restoreFromCache", e.getMessage());
            data = null;
        }

        if (data == null) {
            return null;
        } else {
            return new PricesImpl(exchange, coins, true);
        }
    }

    public static boolean isCacheExist(Context context, String tag) {
        return CacheHelper.exists(context, getCacheName(tag));
    }

    @Override
    public void copyAttrsToCoin(Coin coin) {
        if (coin.isSectionHeader()) {
            return;
        }

        for (PriceMultiFullCoin c : coins) {
            if (coin.getSymbol().equals(c.getFromSymbol())) {
                coin.setPrice(c.getPrice());
                coin.setPriceDiff(c.getChange24Hour());
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
    public String getExchange() {
        return exchange;
    }
}
