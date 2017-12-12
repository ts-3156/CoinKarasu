package com.example.coinkarasu.cryptocompare.response;

import android.content.Context;
import android.util.Log;

import com.example.coinkarasu.utils.CacheHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class PricesResponseImpl implements PricesResponse {

    private static final String CACHE_NAME = "prices_response.json";

    private JSONObject response;

    public PricesResponseImpl(JSONObject response) {
        this.response = response;
    }

    @Override
    public JSONObject getRaw() {
        if (response == null) {
            return null;
        }

        JSONObject raw = null;

        try {
            raw = response.getJSONObject("RAW");
        } catch (JSONException e) {
            Log.d("getRaw", e.getMessage());
            Log.d("getRaw", response.toString());
        }

        return raw;
    }

    @Override
    public boolean saveToCache(Context context) {
        if (response == null) {
            return false;
        }

        CacheHelper.write(context, CACHE_NAME, response.toString());
        return true;
    }

    // @Override
    public static PricesResponse restoreFromCache(Context context) {
        String text = CacheHelper.read(context, CACHE_NAME);
        JSONObject response = null;

        try {
            response = new JSONObject(text);
        } catch (JSONException e) {
            Log.d("restoreFromCache", e.getMessage());
        }

        if (response == null) {
            return null;
        } else {
            return new PricesResponseImpl(response);
        }
    }
}
