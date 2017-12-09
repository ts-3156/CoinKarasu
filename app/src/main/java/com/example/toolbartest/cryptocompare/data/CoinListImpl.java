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
import java.util.Iterator;

public class CoinListImpl implements CoinList {
    private CoinListResponse response;

    public CoinListImpl(CoinListResponse response) {
        this.response = response;
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
    public ArrayList<Coin> collectCoins(String[] fromSymbols) {
        final ArrayList<Coin> coins = new ArrayList<>(fromSymbols.length);

        for (String coinSymbol : fromSymbols) {
            Coin coin = getCoinBySymbol(coinSymbol);
            if (coin == null) {
                continue;
            }

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
}
