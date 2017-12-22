package com.example.coinkarasu.cryptocompare.response;

import android.content.Context;

import org.json.JSONArray;

public interface TopPairsResponse {
    JSONArray getData();

    boolean saveToCache(Context context);

    boolean isCache();

    String getFromSymbol();
}
