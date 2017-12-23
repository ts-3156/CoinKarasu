package com.example.coinkarasu.coins;

public interface TradingOrSalesCoin {
    boolean isTradingCoin();

    boolean isSalesCoin();

    void setCoinKind(CoinImpl.Kind kind);
}

