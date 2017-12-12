package com.example.coinkarasu.coins;

public interface AggregatedData {
    String getMarket();

    String getFromSymbol();

    String getToSymbol();

    double getPrice();

    double getVolume24Hour();
}
