package com.coinkarasu.api.cryptocompare.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class HistoryImpl implements History {
    private long time;
    private double close;
    private double high;
    private double low;
    private double open;
    private double volumeFrom;
    private double volumeTo;

    private String fromSymbol;
    private String toSymbol;

    public HistoryImpl(JSONObject row, String fromSymbol, String toSymbol) {
        this.fromSymbol = fromSymbol;
        this.toSymbol = toSymbol;

        try {
            time = row.getLong("time");
            close = row.getDouble("close");
            high = row.getDouble("high");
            low = row.getDouble("low");
            open = row.getDouble("open");
            volumeFrom = row.getDouble("volumefrom");
            volumeTo = row.getDouble("volumeto");
        } catch (JSONException e) {
            Log.d("HistoryImpl", e.getMessage());
            Log.d("HistoryImpl", row.toString());
        }
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public double getClose() {
        return close;
    }

    @Override
    public double getHigh() {
        return high;
    }

    @Override
    public double getLow() {
        return low;
    }

    @Override
    public double getOpen() {
        return open;
    }

    @Override
    public double getVolumeFrom() {
        return volumeFrom;
    }

    @Override
    public double getVolumeTo() {
        return volumeTo;
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
    public String toString() {
        return "" + time + ", " + close;
    }
}
