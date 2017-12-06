package com.example.toolbartest.cryptocompare.data;

import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.cryptocompare.response.Cacheable;

import java.util.List;

public interface Prices extends Cacheable {
    void setAttrsToCoin(Coin coin);

    void setAttrsToCoins(List<Coin> coins);

    String getExchange();
}
