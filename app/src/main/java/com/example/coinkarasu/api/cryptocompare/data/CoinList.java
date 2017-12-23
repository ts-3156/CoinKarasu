package com.example.coinkarasu.api.cryptocompare.data;

import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.api.cryptocompare.response.Cacheable;

import java.util.ArrayList;

public interface CoinList extends Cacheable {
//    static CoinList buildByResponse(JSONObject response);

//    static CoinList restoreFromCache(Activity activity);

    Coin getCoinBySymbol(String symbol);

    Coin getCoinByCCId(String id);

    //    static CoinListImpl.Builder builder();

    ArrayList<Coin> collectCoins(String[] fromSymbols);

    ArrayList<String> getAllSymbols();

    ArrayList<String> getAllSymbols(int offset, int limit);

    void removeBySymbols(ArrayList<String> symbols);
}