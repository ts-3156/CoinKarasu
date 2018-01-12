package com.coinkarasu.api.cryptocompare.data;

import com.coinkarasu.coins.PriceMultiFullCoin;

import java.util.List;

public interface Prices {
    List<PriceMultiFullCoin> getCoins();

    String getExchange();
}
