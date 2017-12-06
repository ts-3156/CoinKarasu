package com.example.toolbartest.coins;

public interface PriceMultiFullCoin {
    void setPrice(double price);

    double getPrice();

    void setTrend(double trend);

    double getTrend();

    String getToSymbol();

    void setToSymbol(String toSymbol);

    String getExchange();

    void setExchange(String exchange);
}
