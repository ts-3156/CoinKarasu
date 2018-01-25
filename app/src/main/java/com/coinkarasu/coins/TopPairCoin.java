package com.coinkarasu.coins;

public interface TopPairCoin {

    String getExchange();

    String getFromSymbol();

    String getToSymbol();

    double getVolume24h();

    double getVolume24hTo();
}
