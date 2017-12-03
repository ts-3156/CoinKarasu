package com.example.toolbartest.coins;

import android.graphics.Bitmap;

import org.json.JSONObject;

public interface Coin {

    public int getId();

    public Bitmap getIcon();

    public String getUrl();

    public String getImageUrl();

    public String getName();

    public String getSymbol();

    public double getPrice();

    public double getTrend();

    public String getCoinName();

    public String getFullName();

    public String getAlgorithm();

    public String getProofType();

    public String getSortOrder();

    public JSONObject getAttrs();
}
