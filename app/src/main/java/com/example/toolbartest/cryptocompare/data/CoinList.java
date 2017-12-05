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

    //    static CoinListImpl.Builder builder();

    ArrayList<Coin> collectCoins(String[] fromSymbols, String toSymbol);
}
