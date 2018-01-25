package com.coinkarasu.coins;

import com.coinkarasu.utils.CKLog;

import org.json.JSONException;
import org.json.JSONObject;

public class TopPairCoinImpl implements TopPairCoin {
    private static final boolean DEBUG = true;
    private static final String TAG = "TopPairCoinImpl";

    private String exchange;
    private String fromSymbol;
    private String toSymbol;
    private double volume24h;
    private double volume24hTo;

    public TopPairCoinImpl(JSONObject response) {
        if (response == null) {
            return;
        }

        try {
            exchange = response.getString("exchange");
            fromSymbol = response.getString("fromSymbol");
            toSymbol = response.getString("toSymbol");
            volume24h = response.getDouble("volume24h");
            volume24hTo = response.getDouble("volume24hTo");
        } catch (JSONException e) {
            CKLog.e(TAG, response.toString(), e);
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
