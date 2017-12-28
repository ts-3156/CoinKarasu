package com.example.coinkarasu.tasks.by_exchange;

import com.example.coinkarasu.activities.etc.CoinKind;
import com.example.coinkarasu.activities.etc.Exchange;

public class Price {
    Exchange exchange;
    CoinKind coinKind;

    public String fromSymbol;
    public String toSymbol;
    public double price;
    public double priceDiff;
    public double trend;

    public Price(Exchange exchange, CoinKind coinKind, String fromSymbol, String toSymbol, double price) {
        this.exchange = exchange;
        this.coinKind = coinKind;
        this.fromSymbol = fromSymbol;
        this.toSymbol = toSymbol;
        this.price = price;
        this.priceDiff = 0.0;
        this.trend = 0.0;
    }
}
