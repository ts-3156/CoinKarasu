package com.coinkarasu.api.cryptocompare.data;

import com.coinkarasu.coins.Coin;
import com.coinkarasu.api.cryptocompare.response.Cacheable;

import java.util.ArrayList;
import java.util.List;

public interface CoinList extends Cacheable {
//    static CoinList buildByResponse(JSONObject response);

//    static CoinList restoreFromCache(Activity activity);

    Coin getCoinBySymbol(String symbol);

    Coin getCoinByCCId(String id);

    //    static CoinListImpl.Builder builder();

    List<Coin> collectCoins(String[] fromSymbols);

    List<String> getAllSymbols();

    List<String> getAllSymbols(int offset, int limit);

    void removeBySymbols(ArrayList<String> symbols);
}
