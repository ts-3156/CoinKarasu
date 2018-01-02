package com.coinkarasu.api.cryptocompare.data;

import com.coinkarasu.coins.PriceMultiFullCoin;

public interface Price {
    PriceMultiFullCoin getCoin();

    String getExchange();
}
