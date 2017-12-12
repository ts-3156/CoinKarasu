package com.example.coinkarasu.cryptocompare.response;

import org.json.JSONObject;

public interface PricesResponse extends Cacheable {
    JSONObject getRaw();
}
