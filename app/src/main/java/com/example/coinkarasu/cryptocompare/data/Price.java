package com.example.coinkarasu.cryptocompare.data;

import com.example.coinkarasu.coins.PriceMultiFullCoin;

public interface Price {
    PriceMultiFullCoin getCoin();

    String getExchange();
}
