package com.example.toolbartest.cryptocompare;

import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.cryptocompare.data.Prices;

import java.util.ArrayList;

public interface Client {
    ArrayList<Coin> collectCoins(String[] fromSymbols, String toSymbol);

    Prices getPrices(String[] fromSymbols, String toSymbol);

    Prices getPrices(String[] fromSymbols, String toSymbol, String exchange);
}
