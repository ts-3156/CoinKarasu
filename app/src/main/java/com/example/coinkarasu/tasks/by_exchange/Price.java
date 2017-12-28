package com.example.coinkarasu.tasks.by_exchange;

import com.example.coinkarasu.activities.etc.CoinKind;
import com.example.coinkarasu.activities.etc.Exchange;

public class Price {
    Exchange exchange;
    CoinKind coinKind;

    public String fromSymbol;
    public String toSymbol;
    public double value;

    public Price(Exchange exchange, CoinKind coinKind, String fromSymbol, String toSymbol, double value) {
        this.exchange = exchange;
        this.coinKind = coinKind;
        this.fromSymbol = fromSymbol;
        this.toSymbol = toSymbol;
        this.value = value;
    }
}
