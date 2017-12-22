package com.example.coinkarasu.api.cryptocompare.data;

import com.example.coinkarasu.coins.PriceMultiFullCoin;

public interface Price {
    PriceMultiFullCoin getCoin();

    String getExchange();
}
