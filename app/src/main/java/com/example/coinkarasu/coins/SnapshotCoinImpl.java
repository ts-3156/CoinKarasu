package com.example.coinkarasu.coins;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class SnapshotCoinImpl implements SnapshotCoin {
    private String market;
    private String fromSymbol;
    private String toSymbol;
    private double price;
    private long lastUpdate;
    private double lastVolume;
    private double lastVolumeTo;
    private double volume24Hour;
    private double volume24HourTo;
    private double open24Hour;
    private double high24Hour;
    private double low24Hour;

    public SnapshotCoinImpl(JSONObject response) {
        try {
            market = response.getString("MARKET");
            fromSymbol = response.getString("FROMSYMBOL");
            toSymbol = response.getString("TOSYMBOL");
            price = response.getDouble("PRICE");
            lastUpdate = response.getLong("LASTUPDATE");
            lastVolume = response.getDouble("LASTVOLUME");
            lastVolumeTo = response.getDouble("LASTVOLUMETO");
            volume24Hour = response.getDouble("VOLUME24HOUR");
            volume24HourTo = response.getDouble("VOLUME24HOURTO");
            open24Hour = response.getDouble("OPEN24HOUR");
            high24Hour = response.getDouble("HIGH24HOUR");
            low24Hour = response.getDouble("LOW24HOUR");
            lastVolumeTo = response.getDouble("LASTVOLUMETO");
        } catch (JSONException e) {
            Log.e("SnapshotCoinImpl", e.getMessage());
            Log.e("SnapshotCoinImpl", response.toString());
        }
    }

    public static SnapshotCoinImpl buildByJSONObject(JSONObject attrs) {
        return new SnapshotCoinImpl(attrs);
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

    @Override
    public long getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public double getLastVolume() {
        return lastVolume;
    }

    @Override
    public double getLastVolumeTo() {
        return lastVolumeTo;
    }

    @Override
    public double getVolume24HourTo() {
        return volume24HourTo;
    }

    @Override
    public double getOpen24Hour() {
        return open24Hour;
    }

    @Override
    public double getHigh24Hour() {
        return high24Hour;
    }

    @Override
    public double getLow24Hour() {
        return low24Hour;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            json.put("MARKET", market);
            json.put("FROMSYMBOL", fromSymbol);
            json.put("TOSYMBOL", toSymbol);
            json.put("PRICE", price);
            json.put("LASTUPDATE", lastUpdate);
            json.put("LASTVOLUME", lastVolume);
            json.put("LASTVOLUMETO", lastVolumeTo);
            json.put("VOLUME24HOUR", volume24Hour);
            json.put("VOLUME24HOURTO", volume24HourTo);
            json.put("OPEN24HOUR", open24Hour);
            json.put("HIGH24HOUR", high24Hour);
            json.put("LOW24HOUR", low24Hour);
            json.put("LASTVOLUMETO", lastVolumeTo);
        } catch (JSONException e) {
            Log.e("toJson", e.getMessage());
        }

        return json;
    }

}
