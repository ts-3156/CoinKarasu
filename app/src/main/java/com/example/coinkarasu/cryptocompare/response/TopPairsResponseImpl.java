package com.example.coinkarasu.cryptocompare.response;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TopPairsResponseImpl implements TopPairsResponse {

    private JSONObject response;
    private String fromSymbol;

    public TopPairsResponseImpl(JSONObject response, String fromSymbol) {
        this.response = response;
        this.fromSymbol = fromSymbol;
    }

    @Override
    public JSONArray getData() {
        if (response == null) {
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
}
