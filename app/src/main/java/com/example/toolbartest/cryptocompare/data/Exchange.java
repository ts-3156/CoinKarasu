package com.example.toolbartest.cryptocompare.data;

public interface Exchange {
    String getMarket();

    String getFromSymbol();

    String getToSymbol();

    double getPrice();

    double getVolume24Hour();
}
