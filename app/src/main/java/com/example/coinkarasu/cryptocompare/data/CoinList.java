package com.example.coinkarasu.cryptocompare.data;

import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.cryptocompare.response.Cacheable;

import java.util.ArrayList;

public interface CoinList extends Cacheable {
//    static CoinList buildByResponse(JSONObject response);

//    static CoinList restoreFromCache(Activity activity);

    Coin getCoinBySymbol(String symbol);

    Coin getCoinByCCId(String id);

    //    static CoinListImpl.Builder builder();

    ArrayList<Coin> collectCoins(String[] fromSymbols);
}
