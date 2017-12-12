package com.example.coinkarasu.cryptocompare.response;

import org.json.JSONArray;

public interface HistoryResponse {
    JSONArray getData();

    String getFromSymbol();
    String getToSymbol();
}
