package com.example.toolbartest.coins;

import android.content.Context;
import android.graphics.Bitmap;

import org.json.JSONObject;

public interface CoinList {
    com.example.toolbartest.coins.Coin getCoinBySymbol(String symbol);

    com.example.toolbartest.coins.Coin getCoinByCCId(String id);

    String[] getDefaultCCWatchlistIds();

//    static CoinListImpl.Fetcher fetcher();
//    static CoinListImpl.Builder builder();
    boolean saveToFile(Context context);

    interface Listener {
        void finished(CoinList coinList);
    }
}
