package com.example.coinkarasu.api.cryptocompare.response;

import org.json.JSONObject;

public interface PricesResponse extends Cacheable {
    JSONObject getRaw();

    String getExchange();
}
