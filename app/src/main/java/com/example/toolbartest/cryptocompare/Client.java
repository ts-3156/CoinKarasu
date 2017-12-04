package com.example.toolbartest.cryptocompare;

import com.example.toolbartest.coins.CoinList;

import java.util.HashMap;

public interface Client {
    void getCoinList(Client.CoinListListener listener);

    void getCoinPrices(String[] fromSymbols, String toSymbol, Client.CoinPricesListener listener);

    interface CoinListListener {
        void finished(CoinList coinList);
    }

    interface CoinPricesListener {
        void finished(HashMap<String, Double> prices, HashMap<String, Double> trends);
    }
}
