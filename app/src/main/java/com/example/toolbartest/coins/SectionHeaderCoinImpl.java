package com.example.toolbartest.coins;

import android.graphics.Bitmap;

import org.json.JSONObject;

public class SectionHeaderCoinImpl implements Coin {
    private String name;
    private String exchange;

    public SectionHeaderCoinImpl(String name) {
        this.name = name;
        this.exchange = null;
    }

    public SectionHeaderCoinImpl(String name, String exchange) {
        this.name = name;
        this.exchange = exchange;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public Bitmap getIcon() {
        return null;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public String getImageUrl() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSymbol() {
        return null;
    }

    @Override
    public void setPrice(double price) {
    }

    @Override
    public double getPrice() {
        return 0.0;
    }

    @Override
    public void setTrend(double trend) {
    }

    @Override
    public double getTrend() {
        return 0.0;
    }

    @Override
    public String getCoinName() {
        return null;
    }

    @Override
    public String getFullName() {
        return null;
    }

    @Override
    public String getAlgorithm() {
        return null;
    }

    @Override
    public String getProofType() {
        return null;
    }

    @Override
    public String getSortOrder() {
        return null;
    }

    @Override
    public String getToSymbol() {
        return null;
    }

    @Override
    public void setToSymbol(String toSymbol) {
    }

    @Override
    public String getExchange() {
        return exchange;
    }

    @Override
    public void setExchange(String exchange) {
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public double getPrevPrice() {
        return 0.0;
    }

    @Override
    public double getPrevTrend() {
        return 0.0;
    }

    @Override
    public JSONObject toJson() {
        return null;
    }

    @Override
    public boolean isSectionHeader() {
        return true;
    }
}
