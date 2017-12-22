package com.example.coinkarasu.api.cryptocompare.response;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class CoinSnapshotResponseImpl implements CoinSnapshotResponse {

    private JSONObject response;
    private String fromSymbol;
    private String toSymbol;

    public CoinSnapshotResponseImpl(JSONObject response, String fromSymbol, String toSymbol) {
        this.response = response;
        this.fromSymbol = fromSymbol;
        this.toSymbol = toSymbol;
    }

    @Override
    public JSONObject getData() {
        if (response == null) {
            return null;
        }

        JSONObject data = null;

        try {
            data = response.getJSONObject("Data");
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
