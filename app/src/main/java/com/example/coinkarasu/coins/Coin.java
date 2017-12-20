package com.example.coinkarasu.coins;

import org.json.JSONObject;

public interface Coin extends CoinListCoin, PriceMultiFullCoin, SectionHeaderCoin, UpdatableCoin {

    String getImageUrl();

    String getExchange();

    String toString();

    JSONObject toJson();
}
