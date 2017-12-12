package com.example.coinkarasu.cryptocompare.response;

import org.json.JSONArray;

public interface TopPairsResponse {
    JSONArray getData();

    String getFromSymbol();
}
