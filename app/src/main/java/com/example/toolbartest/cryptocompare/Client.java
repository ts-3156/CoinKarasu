package com.example.toolbartest.cryptocompare;

import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.cryptocompare.data.CoinSnapshot;
import com.example.toolbartest.cryptocompare.data.History;
import com.example.toolbartest.cryptocompare.data.Prices;
import com.example.toolbartest.cryptocompare.data.TopPairs;

import java.util.ArrayList;

public interface Client {
    ArrayList<Coin> collectCoins(String[] fromSymbols, String toSymbol);

    Prices getPrices(String[] fromSymbols, String toSymbol);

    Prices getPrices(String[] fromSymbols, String toSymbol, String exchange);

    ArrayList<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit);

    ArrayList<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit, int aggregate);

    ArrayList<History> getHistoryHour(String fromSymbol, String toSymbol, int limit);

    ArrayList<History> getHistoryHour(String fromSymbol, String toSymbol, int limit, int aggregate);

    ArrayList<History> getHistoryDay(String fromSymbol, String toSymbol, int limit);

    ArrayList<History> getHistoryDay(String fromSymbol, String toSymbol, int limit, int aggregate);

    CoinSnapshot getCoinSnapshot(String fromSymbol, String toSymbol);

    TopPairs getTopPairs(String fromSymbol);
}
