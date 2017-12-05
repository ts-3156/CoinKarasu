package com.example.toolbartest.cryptocompare.data;

import com.example.toolbartest.cryptocompare.response.Cacheable;

import java.util.HashMap;

public interface Prices extends Cacheable {
    HashMap<String, Double> getPrices();

    HashMap<String, Double> getTrends();
}
