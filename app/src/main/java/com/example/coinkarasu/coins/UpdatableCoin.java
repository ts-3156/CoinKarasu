package com.example.coinkarasu.coins;

public interface UpdatableCoin {
    void setPrice(double price);

    void setTrend(double trend);

    void setToSymbol(String toSymbol);

    void setExchange(String exchange);
}