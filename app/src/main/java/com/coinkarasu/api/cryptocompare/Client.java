package com.coinkarasu.api.cryptocompare;

import com.coinkarasu.api.cryptocompare.data.CoinSnapshot;
import com.coinkarasu.api.cryptocompare.data.History;
import com.coinkarasu.api.cryptocompare.data.Price;
import com.coinkarasu.api.cryptocompare.data.Prices;
import com.coinkarasu.api.cryptocompare.data.TopPair;

import java.util.List;

public interface Client {
    Price getPrice(String fromSymbol, String toSymbol, String exchange);

    Prices getPrices(String[] fromSymbols, String toSymbol, String exchange);

    List<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit);

    List<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit, int aggregate, String exchange);

    List<History> getHistoryHour(String fromSymbol, String toSymbol, int limit);

    List<History> getHistoryHour(String fromSymbol, String toSymbol, int limit, int aggregate);

    List<History> getHistoryHour(String fromSymbol, String toSymbol, int limit, int aggregate, String exchange);

    List<History> getHistoryDay(String fromSymbol, String toSymbol, int limit);

    List<History> getHistoryDay(String fromSymbol, String toSymbol, int limit, int aggregate);

    List<History> getHistoryDay(String fromSymbol, String toSymbol, int limit, int aggregate, String exchange);

    CoinSnapshot getCoinSnapshot(String fromSymbol, String toSymbol);

    List<TopPair> getTopPairs(String fromSymbol);
}
