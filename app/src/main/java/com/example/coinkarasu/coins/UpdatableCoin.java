package com.example.coinkarasu.coins;

public interface UpdatableCoin {
    void setPrice(double price);

    void setPriceDiff(double trend);

    void setTrend(double trend);

    double getPrice();

    double getPriceDiff();

    double getTrend();

    double getPrevPrice();

    double getPrevPriceDiff();

    double getPrevTrend();

    void setToSymbol(String toSymbol);

    void setExchange(String exchange);
}
