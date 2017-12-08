package com.example.toolbartest.cryptocompare.response;

import org.json.JSONArray;

public interface HistoryResponse {
    JSONArray getData();

    String getFromSymbol();
    String getToSymbol();
}
