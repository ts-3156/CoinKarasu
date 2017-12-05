package com.example.toolbartest.cryptocompare;

import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.cryptocompare.data.CoinList;
import com.example.toolbartest.cryptocompare.data.Prices;

import java.util.ArrayList;

public interface Client {
    ArrayList<Coin> getCoins(String[] fromSymbols, String toSymbol);

    void getCoins(String[] fromSymbols, String toSymbol, CoinsListener listener);

    ArrayList<Coin> collectCoins(String[] fromSymbols, String toSymbol);

    void getCoinList(String[] fromSymbols, String toSymbol, CoinListListener listener);

    Prices getPrices(String[] fromSymbols, String toSymbol);

    void getPrices(String[] fromSymbols, String toSymbol, PricesListener listener);

    interface CoinsListener {
        void finished(ArrayList<Coin> coins);
    }

    interface CoinListListener {
        void finished(CoinList coinList);
    }

    interface PricesListener {
        void finished(Prices prices);
    }
}
