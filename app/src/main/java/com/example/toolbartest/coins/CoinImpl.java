package com.example.toolbartest.coins;

import android.graphics.Bitmap;

import org.json.JSONException;
import org.json.JSONObject;

public class CoinImpl implements Coin {
    private static final String BASE_URL = "https://www.cryptocompare.com";

    private JSONObject attrs;
    private int id;
    private Bitmap icon;
    private String url;
    private String imageUrl;
    private String name;
    private String coinName;
    private String fullName;
    private String algorithm;
    private String proofType;
    private String sortOrder;

    private String toSymbol;
    private double price;
    private double trend;

    private double prevPrice;
    private double prevTrend;

    private CoinImpl(JSONObject attrs) {
        this.attrs = attrs;
        this.icon = null;

        try {
            this.id = Integer.parseInt((String) attrs.get("Id"));
            this.url = (String) attrs.get("Url");
            this.imageUrl = (String) attrs.get("ImageUrl");
            this.name = (String) attrs.get("Name");
            this.coinName = (String) attrs.get("CoinName");
            this.fullName = (String) attrs.get("FullName");
            this.algorithm = (String) attrs.get("Algorithm");
            this.proofType = (String) attrs.get("ProofType");
            this.sortOrder = (String) attrs.get("SortOrder");
        } catch (JSONException e) {
        }

        this.price = 0.0;
        this.trend = 0.0;
        this.prevPrice = 0.0;
        this.prevTrend = 0.0;
    }

    public static Coin buildByJSONObject(JSONObject attrs) {
        return new CoinImpl(attrs);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Bitmap getIcon() {
        return icon;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getImageUrl() {
        return BASE_URL + imageUrl + "?width=192";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSymbol() {
        return getName();
    }

    @Override
    public void setPrice(double price) {
        prevPrice = this.price;
        this.price = price;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public void setTrend(double trend) {
        prevTrend = this.trend;
        this.trend = trend;
    }

    @Override
    public double getTrend() {
        return trend;
    }

    @Override
    public String getCoinName() {
        return coinName;
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public String getProofType() {
        return proofType;
    }

    @Override
    public String getSortOrder() {
        return sortOrder;
    }

    @Override
    public String getToSymbol() {
        return toSymbol;
    }

    @Override
    public void setToSymbol(String toSymbol) {
        this.toSymbol = toSymbol;
    }

    @Override
    public String toString() {
        return attrs.toString();
    }

    @Override
    public double getPrevPrice() {
        return prevPrice;
    }

    @Override
    public double getPrevTrend() {
        return prevTrend;
    }

    @Override
    public boolean isSectionHeader() {
        return false;
    }
}
