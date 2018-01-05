package com.coinkarasu.api.cryptocompare.response;

import android.content.Context;
import android.util.Log;

import com.coinkarasu.api.cryptocompare.data.History;
import com.coinkarasu.api.cryptocompare.data.HistoryImpl;
import com.coinkarasu.utils.DiskCacheHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HistoryResponseImpl implements HistoryResponse {

    public enum Kind {
        minute(60 * 1000),
        hour(60 * 60 * 1000),
        day(24 * 60 * 60 * 1000);

        long expires;

        Kind(long expires) {
            this.expires = expires;
        }
    }

    private JSONObject response;

    private boolean isCache;
    private String fromSymbol;
    private String toSymbol;
    private Kind kind;
    private int limit;
    private String exchange;

    public HistoryResponseImpl(JSONObject response, String fromSymbol, String toSymbol, Kind kind, int limit, String exchange) {
        this(response, fromSymbol, toSymbol, kind, limit, exchange, false);
    }

    private HistoryResponseImpl(JSONObject response, String fromSymbol, String toSymbol, Kind kind, int limit, String exchange, boolean isCache) {
        this.response = response;

        this.isCache = isCache;
        this.fromSymbol = fromSymbol;
        this.toSymbol = toSymbol;
        this.kind = kind;
        this.limit = limit;
        this.exchange = exchange;
    }

    @Override
    public JSONArray getData() {
        if (response == null) {
            Log.d("getData", "Response is null.");
            return null;
        }

        JSONArray data = null;

        try {
            data = response.getJSONArray("Data");
        } catch (JSONException e) {
            Log.e("getData", e.getMessage() + ", " + response.toString());
        }

        return data;
    }

    @Override
    public ArrayList<History> getHistories() {
        JSONArray data = getData();
        if (data == null) {
            Log.d("getHistories", "null");
            return null;
        }

        ArrayList<History> histories = new ArrayList<>();

        try {
            for (int i = 0; i < data.length(); i++) {
                histories.add(new HistoryImpl(data.getJSONObject(i), fromSymbol, toSymbol));
            }
        } catch (JSONException e) {
            Log.e("getHistories", e.getMessage() + ", " + data.toString());
            histories = null;
        }

        return histories;
    }

    private static String getCacheName(String fromSymbol, String toSymbol, Kind kind, int limit, String exchange) {
        return "history_response_" + fromSymbol + "_" + toSymbol + "_" + kind + "_" + limit + "_" + exchange + ".json";
    }

    @Override
    public boolean saveToCache(Context context) {
        if (response == null) {
            return false;
        }

        DiskCacheHelper.write(context, getCacheName(fromSymbol, toSymbol, kind, limit, exchange), response.toString());
        return true;
    }

    @Override
    public boolean saveToCache(Context context, String tag) {
        return saveToCache(context);
    }

    public static HistoryResponse restoreFromCache(Context context, String fromSymbol, String toSymbol, Kind kind, int limit, String exchange) {
        String text = DiskCacheHelper.read(context, getCacheName(fromSymbol, toSymbol, kind, limit, exchange));
        JSONObject response = null;

        try {
            response = new JSONObject(text);
        } catch (JSONException e) {
            Log.e("restoreFromCache", e.getMessage() + ", " + text);
        }

        if (response == null) {
            return null;
        }

        return new HistoryResponseImpl(response, fromSymbol, toSymbol, kind, limit, exchange, true);
    }

    public static boolean isCacheExist(Context context, String fromSymbol, String toSymbol, Kind kind, int limit, String exchange) {
        boolean exists = DiskCacheHelper.exists(context, getCacheName(fromSymbol, toSymbol, kind, limit, exchange));
        if (!exists) {
            return false;
        }

        return !DiskCacheHelper.isExpired(context, getCacheName(fromSymbol, toSymbol, kind, limit, exchange), kind.expires);
    }

    @Override
    public boolean isCache() {
        return isCache;
    }
}
