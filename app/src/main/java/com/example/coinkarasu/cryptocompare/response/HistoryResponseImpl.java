package com.example.coinkarasu.cryptocompare.response;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HistoryResponseImpl implements HistoryResponse {

    private JSONObject response;
    private String fromSymbol;
    private String toSymbol;

    public HistoryResponseImpl(JSONObject response, String fromSymbol, String toSymbol) {
        this.response = response;
        this.fromSymbol = fromSymbol;
        this.toSymbol = toSymbol;
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
            Log.d("getData", e.getMessage());
            Log.d("getData", response.toString());
        }

        return data;
    }

    @Override
    public String getFromSymbol() {
        return fromSymbol;
    }

    @Override
    public String getToSymbol() {
        return toSymbol;
    }
}
