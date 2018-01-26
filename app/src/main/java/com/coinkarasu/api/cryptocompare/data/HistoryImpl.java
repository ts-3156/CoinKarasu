package com.coinkarasu.api.cryptocompare.data;

import com.coinkarasu.utils.CKLog;

import org.json.JSONException;
import org.json.JSONObject;

public class HistoryImpl implements History {
    private static final String TAG = "HistoryImpl";
    private static final boolean DEBUG = CKLog.DEBUG;

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
        if (row == null) {
            if (DEBUG) CKLog.w(TAG, "HistoryImpl() row is null.");
            return;
        }

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
            CKLog.e(TAG, row.toString(), e);
        }
    }

    public static History buildByJson(JSONObject data) {
        History history = null;

        try {
            String fromSymbol = data.getString("fromSymbol");
            String toSymbol = data.getString("toSymbol");

            history = new HistoryImpl(data, fromSymbol, toSymbol);
        } catch (JSONException e) {
            CKLog.e(TAG, data.toString(), e);
        }

        return history;
    }

    public static History buildByString(String data) {
        History history;

        try {
            history = buildByJson(new JSONObject(data));
        } catch (JSONException e) {
            CKLog.e(TAG, data, e);
            history = null;
        }

        return history;
    }

    public JSONObject toJson() {
        JSONObject data = new JSONObject();

        try {
            data.put("time", time);
            data.put("close", close);
            data.put("high", high);
            data.put("low", low);
            data.put("open", open);
            data.put("volumefrom", volumeFrom);
            data.put("volumeto", volumeTo);

            data.put("fromSymbol", fromSymbol);
            data.put("toSymbol", toSymbol);
        } catch (JSONException e) {
            CKLog.e(TAG, e);
        }

        return data;
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

    public String toString() {
        return toJson().toString();
    }
}
