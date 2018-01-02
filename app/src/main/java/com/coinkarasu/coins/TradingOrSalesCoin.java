package com.coinkarasu.coins;

import com.coinkarasu.activities.etc.CoinKind;

public interface TradingOrSalesCoin {
    boolean isTradingCoin();

    boolean isSalesCoin();

    void setCoinKind(CoinKind coinKind);

    CoinKind getCoinKind();
}

