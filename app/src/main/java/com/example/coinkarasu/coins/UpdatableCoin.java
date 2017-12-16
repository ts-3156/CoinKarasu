package com.example.coinkarasu.coins;

public interface UpdatableCoin {
    void setPrice(double price);

    void setTrend(double trend);

    double getPrice();

    double getTrend();

    double getPrevPrice();

    double getPrevTrend();

    void setToSymbol(String toSymbol);

    void setExchange(String exchange);
}
