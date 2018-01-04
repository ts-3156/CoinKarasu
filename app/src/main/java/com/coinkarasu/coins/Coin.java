package com.coinkarasu.coins;

import org.json.JSONObject;

public interface Coin extends CoinListCoin, PriceMultiFullCoin, SectionHeaderCoin, AdCoin, UpdatableCoin, TradingOrSalesCoin {

    String getExchange();

    boolean isChanged();

    String toString();

    JSONObject toJson();
}
