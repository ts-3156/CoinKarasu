package com.example.toolbartest.coins;

import android.graphics.Bitmap;

import org.json.JSONObject;

public interface Coin extends CoinListCoin, PriceMultiFullCoin {

    Bitmap getIcon();

    String getSymbol();

    String toString();
}
