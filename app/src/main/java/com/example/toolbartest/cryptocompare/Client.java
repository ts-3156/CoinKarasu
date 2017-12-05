package com.example.toolbartest.cryptocompare;

import com.example.toolbartest.cryptocompare.data.CoinList;

public interface Client {
    void getCoinList(String[] fromSymbols, String toSymbol, CoinListListener listener);

    interface CoinListListener {
        void finished(CoinList coinList);
    }
}
