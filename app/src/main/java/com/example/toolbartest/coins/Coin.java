package com.example.toolbartest.coins;

import android.graphics.Bitmap;

import org.json.JSONObject;

public interface Coin {

    int getId();

    Bitmap getIcon();

    String getUrl();

    String getImageUrl();

    String getName();

    String getSymbol();

    void setPrice(double price);

    double getPrice();

    double getTrend();

    String getCoinName();

    String getFullName();

    String getAlgorithm();

    String getProofType();

    String getSortOrder();

    JSONObject getAttrs();
}
