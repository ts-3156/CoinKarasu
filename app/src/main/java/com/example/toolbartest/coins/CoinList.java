package com.example.toolbartest.coins;

import android.content.Context;

public interface CoinList {
    Coin getCoinBySymbol(String symbol);

    Coin getCoinByCCId(String id);

    String[] getDefaultCCWatchlistIds();

//    static CoinListImpl.Fetcher fetcher();
//    static CoinListImpl.Builder builder();
    boolean saveToFile(Context context);
}
