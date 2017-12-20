package com.example.coinkarasu.coins;

import org.json.JSONObject;

public interface Coin extends CoinListCoin, PriceMultiFullCoin, SectionHeaderCoin, UpdatableCoin {

    String getExchange();

    String toString();

    JSONObject toJson();
}
