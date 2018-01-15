package com.coinkarasu.activities.etc;

import com.coinkarasu.coins.Coin;

import java.io.Serializable;

public class Section implements Serializable {
    private static final long serialVersionUID = 1L;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (obj instanceof Section) {
            Section other = (Section) obj;
            return exchange == other.getExchange() && coinKind == other.getCoinKind();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public String toString() {
        return "Section(" + exchange.name() + ", " + coinKind.name() + ")";
    }
}
