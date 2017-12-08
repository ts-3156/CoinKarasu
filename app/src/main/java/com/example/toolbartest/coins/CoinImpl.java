package com.example.toolbartest.coins;

import android.graphics.Bitmap;
import android.util.Log;

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
    private String exchange;

    private double prevPrice;
    private double prevTrend;

    private CoinImpl(JSONObject attrs) {
        this.attrs = attrs;
        this.icon = null;

        try {
            this.id = attrs.getInt("Id");
            this.url = attrs.getString("Url");
            this.imageUrl = attrs.getString("ImageUrl");
            this.name = attrs.getString("Name");
            this.coinName = attrs.getString("CoinName"); // BTC
            this.fullName = attrs.getString("FullName");
            this.algorithm = attrs.getString("Algorithm");
            this.proofType = attrs.getString("ProofType");
            this.sortOrder = attrs.getString("SortOrder");

            if (attrs.has("toSymbol")) {
                toSymbol = attrs.getString("toSymbol");
            }
        } catch (JSONException e) {
            Log.d("CoinImpl", e.getMessage());
        }

        this.price = 0.0;
        this.trend = 0.0;
        this.exchange = null;
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
    public String getExchange() {
        return exchange;
    }

    @Override
    public void setExchange(String exchange) {
        this.exchange = exchange;
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
    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            json.put("Id", id);
            json.put("Url", url);
            json.put("ImageUrl", imageUrl);
            json.put("Name", name);
            json.put("CoinName", coinName);
            json.put("FullName", fullName);
            json.put("Algorithm", algorithm);
            json.put("ProofType", proofType);
            json.put("SortOrder", sortOrder);

            json.put("toSymbol", toSymbol);
        } catch (JSONException e) {
            Log.d("toJson", e.getMessage());
        }

        return json;
    }

    @Override
    public boolean isSectionHeader() {
        return false;
    }
}
