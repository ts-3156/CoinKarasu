package com.example.coinkarasu.api.cryptocompare.response;

import org.json.JSONObject;

public interface CoinSnapshotResponse {
    JSONObject getData();

    String getFromSymbol();
    String getToSymbol();
}
