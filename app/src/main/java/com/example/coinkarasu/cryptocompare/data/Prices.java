package com.example.coinkarasu.cryptocompare.data;

import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.cryptocompare.response.Cacheable;

import java.util.HashMap;
import java.util.List;

public interface Prices extends Cacheable {
    void merge(Prices prices);

    void setAttrsToCoin(Coin coin);

    HashMap<String, Double> getPrices();

    HashMap<String, Double> getTrends();

    void setAttrsToCoins(List<Coin> coins);

    String getExchange();
}
