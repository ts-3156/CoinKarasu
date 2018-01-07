package com.coinkarasu.api.cryptocompare.data;

import com.coinkarasu.coins.Coin;
import com.coinkarasu.coins.PriceMultiFullCoin;
import com.coinkarasu.api.cryptocompare.response.Cacheable;

import java.util.List;

public interface Prices extends Cacheable {
    void merge(Prices prices);

    void copyAttrsToCoin(Coin coin);

    List<PriceMultiFullCoin> getCoins();

    void copyAttrsToCoins(List<Coin> coins);

    String getExchange();
}
