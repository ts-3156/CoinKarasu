package com.example.coinkarasu.coins;

import org.json.JSONObject;

public interface Coin extends CoinListCoin, PriceMultiFullCoin, SectionHeaderCoin, UpdatableCoin, TradingOrSalesCoin {

    String getExchange();

    boolean isChanged();

    String toString();

    JSONObject toJson();
}
