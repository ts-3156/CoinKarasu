package com.example.coinkarasu.api.cryptocompare.response;

import org.json.JSONObject;

public interface CoinListResponse extends Cacheable {
    JSONObject getData();

    boolean isSuccess();
}
