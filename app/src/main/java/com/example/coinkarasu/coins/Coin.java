package com.example.coinkarasu.coins;

import org.json.JSONObject;

public interface Coin extends CoinListCoin, PriceMultiFullCoin, SectionHeaderCoin, UpdatableCoin {

    String toString();

    double getPrevPrice();

    double getPrevTrend();

    JSONObject toJson();
}
