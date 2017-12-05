package com.example.toolbartest.cryptocompare.data;

import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.cryptocompare.response.Cacheable;

import java.util.ArrayList;
import java.util.HashMap;

public interface Prices extends Cacheable {
    HashMap<String, Double> getPrices();

    HashMap<String, Double> getTrends();

    void setPriceAndTrendToCoin(Coin coin);

    void setPriceAndTrendToCoins(ArrayList<Coin> coins);
}
