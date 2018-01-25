package com.coinkarasu.api.cryptocompare;

import com.coinkarasu.api.cryptocompare.data.CoinSnapshot;
import com.coinkarasu.api.cryptocompare.data.History;
import com.coinkarasu.api.cryptocompare.data.Price;
import com.coinkarasu.api.cryptocompare.data.Prices;
import com.coinkarasu.api.cryptocompare.data.TopPairs;

import java.util.List;

public interface Client {
    Price getPrice(String fromSymbol, String toSymbol, String exchange);

    Prices getPrices(String[] fromSymbols, String toSymbol, String exchange);

    List<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit, int sampling, String exchange, int mode);

    List<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit, int mode);

    List<History> getHistoryHour(String fromSymbol, String toSymbol, int limit, int sampling, int mode);

    List<History> getHistoryHour(String fromSymbol, String toSymbol, int limit, int mode);

    List<History> getHistoryHour(String fromSymbol, String toSymbol, int limit, int sampling, String exchange, int mode);

    List<History> getHistoryDay(String fromSymbol, String toSymbol, int limit, int sampling, int mode);

    List<History> getHistoryDay(String fromSymbol, String toSymbol, int limit, int sampling, String exchange, int mode);

    CoinSnapshot getCoinSnapshot(String fromSymbol, String toSymbol);

    TopPairs getTopPairs(String fromSymbol);
}
