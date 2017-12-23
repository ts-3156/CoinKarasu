package com.example.coinkarasu.coins;

import org.json.JSONObject;

public interface Coin extends CoinListCoin, PriceMultiFullCoin, SectionHeaderCoin, UpdatableCoin, TradingOrSalesCoin {

    String getExchange();

    String toString();

    JSONObject toJson();
}
