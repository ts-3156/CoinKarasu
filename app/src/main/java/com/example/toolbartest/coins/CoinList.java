package com.example.toolbartest.coins;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public interface CoinList {
    com.example.toolbartest.coins.Coin getCoinBySymbol(String symbol);

    com.example.toolbartest.coins.Coin getCoinByCCId(String id);

    ArrayList<Coin> collectCoins();

    //    static CoinListImpl.Builder builder();
    boolean saveToFile(Context context);

    void setPrices(HashMap<String, Double> prices);

    void setTrends(HashMap<String, Double> trends);

    void setFromSymbols(String[] fromSymbols);

    void setToSymbol(String toSymbol);

    void updatePrices(Activity activity, UpdatePricesListener listener);

    interface UpdatePricesListener {
        void finished();
    }
}
