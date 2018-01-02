package com.coinkarasu.api.cryptocompare.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class TopPairImpl implements TopPair {

    private String exchange;
    private String fromSymbol;
    private String toSymbol;
    private double volume24h;
    private double volume24hTo;

    TopPairImpl(JSONObject response) {
        try {
            exchange = response.getString("exchange");
            fromSymbol = response.getString("fromSymbol");
            toSymbol = response.getString("toSymbol");
            volume24h = response.getDouble("volume24h");
            volume24hTo = response.getDouble("volume24hTo");
        } catch (JSONException e) {
            Log.d("TopPairImpl", e.getMessage());
        }
    }

    @Override
    public String getExchange() {
        return exchange;
    }

    @Override
    public String getFromSymbol() {
        return fromSymbol;
    }

    @Override
    public String getToSymbol() {
        return toSymbol;
    }

    @Override
    public double getVolume24h() {
        return volume24h;
    }

    @Override
    public double getVolume24hTo() {
        return volume24hTo;
    }
}
