package com.example.toolbartest.cryptocompare.data;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.cryptocompare.response.Cacheable;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public interface CoinList extends Cacheable {
//    static CoinList buildByResponse(JSONObject response);

//    static CoinList restoreFromCache(Activity activity);

    com.example.toolbartest.coins.Coin getCoinBySymbol(String symbol);

    com.example.toolbartest.coins.Coin getCoinByCCId(String id);

    ArrayList<Coin> collectCoins();

    //    static CoinListImpl.Builder builder();

    void setPrices(HashMap<String, Double> prices);

    void setTrends(HashMap<String, Double> trends);

    void setFromSymbols(String[] fromSymbols);

    void setToSymbol(String toSymbol);

    void updatePrices(Activity activity, UpdatePricesListener listener);

    interface UpdatePricesListener {
        void finished();
    }
}
