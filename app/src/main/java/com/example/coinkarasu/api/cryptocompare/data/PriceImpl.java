package com.example.coinkarasu.api.cryptocompare.data;

import android.util.Log;

import com.example.coinkarasu.coins.PriceMultiFullCoin;
import com.example.coinkarasu.coins.PriceMultiFullCoinImpl;
import com.example.coinkarasu.api.cryptocompare.response.PricesResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class PriceImpl implements Price {
    private PriceMultiFullCoin coin;
    private String exchange;

    public PriceImpl(PricesResponse response) {
        exchange = response.getExchange();

        JSONObject raw = response.getRaw();
        if (raw == null) {
            Log.e("PriceImpl", response.toString());
            return;
        }

        try {
            String fromSymbol = raw.keys().next();
            JSONObject values = raw.getJSONObject(fromSymbol);

            String toSymbol = values.keys().next();
            JSONObject attrs = values.getJSONObject(toSymbol);

            coin = new PriceMultiFullCoinImpl(attrs);
        } catch (JSONException e) {
            Log.e("PriceImpl", e.getMessage());
            Log.e("PriceImpl", response.toString());
        }
    }

    @Override
    public PriceMultiFullCoin getCoin() {
        return coin;
    }

    @Override
    public String getExchange() {
        return exchange;
    }
}