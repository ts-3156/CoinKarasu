package com.example.coinkarasu.coins;

import org.json.JSONObject;

public interface Coin extends CoinListCoin, PriceMultiFullCoin, SectionHeaderCoin, UpdatableCoin {

    double getTrend();

    String getExchange();

    String toString();

    double getPrevPrice();

    double getPrevTrend();

    JSONObject toJson();
}
