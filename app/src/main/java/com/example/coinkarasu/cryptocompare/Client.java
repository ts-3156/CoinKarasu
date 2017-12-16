package com.example.coinkarasu.cryptocompare;

import com.example.coinkarasu.coins.PriceMultiFullCoin;
import com.example.coinkarasu.cryptocompare.data.CoinList;
import com.example.coinkarasu.cryptocompare.data.CoinSnapshot;
import com.example.coinkarasu.cryptocompare.data.History;
import com.example.coinkarasu.cryptocompare.data.Price;
import com.example.coinkarasu.cryptocompare.data.Prices;
import com.example.coinkarasu.cryptocompare.data.TopPairs;

import java.util.ArrayList;

public interface Client {
    Price getPrice(String fromSymbol, String toSymbol, String exchange);

    Prices getPrices(String[] fromSymbols, String toSymbol);

    Prices getPrices(String[] fromSymbols, String toSymbol, String exchange);

    ArrayList<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit);

    ArrayList<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit, int aggregate, String exchange);

    ArrayList<History> getHistoryHour(String fromSymbol, String toSymbol, int limit);

    ArrayList<History> getHistoryHour(String fromSymbol, String toSymbol, int limit, int aggregate);

    ArrayList<History> getHistoryDay(String fromSymbol, String toSymbol, int limit);

    ArrayList<History> getHistoryDay(String fromSymbol, String toSymbol, int limit, int aggregate);

    CoinSnapshot getCoinSnapshot(String fromSymbol, String toSymbol);

    TopPairs getTopPairs(String fromSymbol);

    ArrayList<PriceMultiFullCoin> getBtcToplist(CoinList coinList);
}
