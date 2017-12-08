package com.example.toolbartest.cryptocompare.response;

import org.json.JSONObject;

public interface CoinSnapshotResponse {
    JSONObject getData();

    String getFromSymbol();
    String getToSymbol();
}
