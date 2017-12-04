package com.example.toolbartest.cryptocompare;

import com.example.toolbartest.coins.CoinList;

public interface Client {
    void getCoinList(ClientImpl.CoinListListener listener);

    void getCoinPrices(String[] fromSymbols, String toSymbol, ClientImpl.CoinPricesListener listener);
}
