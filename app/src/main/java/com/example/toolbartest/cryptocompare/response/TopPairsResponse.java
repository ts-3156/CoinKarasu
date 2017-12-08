package com.example.toolbartest.cryptocompare.response;

import org.json.JSONArray;

public interface TopPairsResponse {
    JSONArray getData();

    String getFromSymbol();
}
