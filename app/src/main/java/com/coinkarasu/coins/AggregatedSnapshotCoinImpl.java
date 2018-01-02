package com.coinkarasu.coins;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class AggregatedSnapshotCoinImpl implements AggregatedSnapshotCoin {
    private String market;
    private String fromSymbol;
    private String toSymbol;
    private double price;
    private long lastUpdate;
    private double lastVolume;
    private double lastVolumeTo;
    private double volumeDay;
    private double volumeDayTo;
    private double volume24Hour;
    private double volume24HourTo;
    private double openDay;
    private double highDay;
    private double lowDay;
    private double open24Hour;
    private double high24Hour;
    private double low24Hour;
    private String lastMarket;

    public AggregatedSnapshotCoinImpl(JSONObject response) {
        try {
            market = response.getString("MARKET");
            fromSymbol = response.getString("FROMSYMBOL");
            toSymbol = response.getString("TOSYMBOL");
            price = response.getDouble("PRICE");
            lastUpdate = response.getLong("LASTUPDATE");
            lastVolume = response.getDouble("LASTVOLUME");
            lastVolumeTo = response.getDouble("LASTVOLUMETO");

            volumeDay = response.getDouble("VOLUMEDAY");
            volumeDayTo = response.getDouble("VOLUMEDAYTO");

            volume24Hour = response.getDouble("VOLUME24HOUR");
            volume24HourTo = response.getDouble("VOLUME24HOURTO");

            openDay = response.getDouble("OPEN24HOUR");
            highDay = response.getDouble("HIGH24HOUR");
            lowDay = response.getDouble("LOW24HOUR");

            open24Hour = response.getDouble("OPEN24HOUR");
            high24Hour = response.getDouble("HIGH24HOUR");
            low24Hour = response.getDouble("LOW24HOUR");

            lastMarket = response.getString("LASTMARKET");
        } catch (JSONException e) {
            Log.e("AggrSnapshotCoinImpl", e.getMessage());
            Log.e("AggrSnapshotCoinImpl", response.toString());
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
    public double getVolumeDay() {
        return volumeDay;
    }

    @Override
    public double getVolumeDayTo() {
        return volumeDayTo;
    }

    @Override
    public double getVolume24HourTo() {
        return volume24HourTo;
    }

    @Override
    public double getOpenDay() {
        return openDay;
    }

    @Override
    public double getHighDay() {
        return highDay;
    }

    @Override
    public double getLowDay() {
        return lowDay;
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
    public String getLastMarket() {
        return lastMarket;
    }

    @Override
    public JSONObject toJson() {
        throw new RuntimeException("Stub");
    }
}
