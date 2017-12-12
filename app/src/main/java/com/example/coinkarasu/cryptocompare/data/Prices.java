package com.example.coinkarasu.cryptocompare.data;

import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.cryptocompare.response.Cacheable;

import java.util.List;

public interface Prices extends Cacheable {
    void setAttrsToCoin(Coin coin);

    void setAttrsToCoins(List<Coin> coins);

    String getExchange();
}
