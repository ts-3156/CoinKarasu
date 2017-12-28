package com.example.coinkarasu.coins;

import com.example.coinkarasu.activities.etc.CoinKind;

public interface TradingOrSalesCoin {
    boolean isTradingCoin();

    boolean isSalesCoin();

    void setCoinKind(CoinKind coinKind);
}

