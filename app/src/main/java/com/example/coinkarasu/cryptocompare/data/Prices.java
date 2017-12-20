package com.example.coinkarasu.cryptocompare.data;

import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.coins.PriceMultiFullCoin;
import com.example.coinkarasu.cryptocompare.response.Cacheable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface Prices extends Cacheable {
    void merge(Prices prices);

    void copyAttrsToCoin(Coin coin);

    ArrayList<PriceMultiFullCoin> getCoins();

    void copyAttrsToCoins(List<Coin> coins);

    String getExchange();
}
