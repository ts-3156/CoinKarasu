package com.coinkarasu.api.cryptocompare;

import com.coinkarasu.api.cryptocompare.data.CoinSnapshot;
import com.coinkarasu.api.cryptocompare.data.History;
import com.coinkarasu.api.cryptocompare.data.Price;
import com.coinkarasu.api.cryptocompare.data.Prices;
import com.coinkarasu.api.cryptocompare.data.TopPair;

import java.util.ArrayList;

public interface Client {
    Price getPrice(String fromSymbol, String toSymbol, String exchange);

    Prices getPrices(String[] fromSymbols, String toSymbol, String exchange);

    ArrayList<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit);

    ArrayList<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit, int aggregate, String exchange);

    ArrayList<History> getHistoryHour(String fromSymbol, String toSymbol, int limit);

    ArrayList<History> getHistoryHour(String fromSymbol, String toSymbol, int limit, int aggregate);

    ArrayList<History> getHistoryHour(String fromSymbol, String toSymbol, int limit, int aggregate, String exchange);

    ArrayList<History> getHistoryDay(String fromSymbol, String toSymbol, int limit);

    ArrayList<History> getHistoryDay(String fromSymbol, String toSymbol, int limit, int aggregate);

    ArrayList<History> getHistoryDay(String fromSymbol, String toSymbol, int limit, int aggregate, String exchange);

    CoinSnapshot getCoinSnapshot(String fromSymbol, String toSymbol);

    ArrayList<TopPair> getTopPairs(String fromSymbol);
}
