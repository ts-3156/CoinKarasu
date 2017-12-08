package com.example.toolbartest.coins;

import org.json.JSONException;
import org.json.JSONObject;

public class AggregatedDataImpl implements AggregatedData {
    private String market;
    private String fromSymbol;
    private String toSymbol;
    private double price;
    private double volume24Hour;

    public AggregatedDataImpl(JSONObject response) {
        try {
            market = response.getString("MARKET");
            fromSymbol = response.getString("FROMSYMBOL");
            toSymbol = response.getString("TOSYMBOL");
            price = response.getDouble("PRICE");
            volume24Hour = response.getDouble("VOLUME24HOUR");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getMarket() {
        return market;
    }

    @Override
    public String getFromSymbol() {
        return fromSymbol;
    }

    @Override
    public String getToSymbol() {
        return toSymbol;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public double getVolume24Hour() {
        return volume24Hour;
    }
}
