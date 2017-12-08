package com.example.toolbartest.cryptocompare.data;

import android.util.Log;

import com.example.toolbartest.coins.AggregatedData;
import com.example.toolbartest.coins.AggregatedDataImpl;
import com.example.toolbartest.cryptocompare.response.CoinSnapshotResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TopPairImpl implements TopPair {

    private String exchange;
    private String fromSymbol;
    private String toSymbol;
    private double volume24h;
    private double volume24hTo;

    public TopPairImpl(JSONObject response) {
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
