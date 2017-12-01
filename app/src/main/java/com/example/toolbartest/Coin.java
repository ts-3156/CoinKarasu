package com.example.toolbartest;

import android.graphics.Bitmap;

public class Coin {
    long id;
    private Bitmap icon;
    private String name;
    private String symbol;
    private double price;
    private double trend;

    public Coin(Bitmap icon, String name, String symbol, double price, double trend) {
        this.icon = icon;
        this.name = name;
        this.symbol = symbol;
        this.price = price;
        this.trend = trend;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getTrend() {
        return trend;
    }

    public void setTrend(double trend) {
        this.trend = trend;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }
}