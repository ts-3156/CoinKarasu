package com.example.toolbartest.cryptocompare;

import com.example.toolbartest.coins.CoinList;

public interface Client {
    void getCoinList(String[] fromSymbols, String toSymbol, CoinListListener listener);

    interface CoinListListener {
        void finished(CoinList coinList);
    }
}
