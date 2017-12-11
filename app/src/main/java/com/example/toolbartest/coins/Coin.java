package com.example.toolbartest.coins;

import org.json.JSONObject;

public interface Coin extends CoinListCoin, PriceMultiFullCoin, SectionHeaderCoin {

    String toString();

    double getPrevPrice();

    double getPrevTrend();

    JSONObject toJson();
}
