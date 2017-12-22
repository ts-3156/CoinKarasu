package com.example.coinkarasu.api.cryptocompare.data;

public interface TopPair {

    String getExchange();

    String getFromSymbol();

    String getToSymbol();

    double getVolume24h();

    double getVolume24hTo();
}
