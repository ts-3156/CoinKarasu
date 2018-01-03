package com.coinkarasu.activities.etc;

import com.coinkarasu.coins.Coin;

public class Section {
    private final Exchange exchange;
    private final CoinKind coinKind;

    public Section(Exchange exchange, CoinKind coinKind) {
        this.exchange = exchange;
        this.coinKind = coinKind;
    }

    public Coin createSectionHeaderCoin() {
        return exchange.createSectionHeaderCoin(coinKind);
    }

    public int getSymbolsResId() {
        int resId;

        switch (coinKind) {
            case none:
                resId = exchange.tradingSymbolsResId;
                break;
            case trading:
                resId = exchange.tradingSymbolsResId;
                break;
            case sales:
                resId = exchange.salesSymbolsResId;
                break;
            default:
                throw new RuntimeException("Invalid coinKind " + coinKind.name());
        }

        return resId;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public CoinKind getCoinKind() {
        return coinKind;
    }

    public String toString() {
        return "Section(" + exchange.name() + ", " + coinKind.name() + ")";
    }
}
