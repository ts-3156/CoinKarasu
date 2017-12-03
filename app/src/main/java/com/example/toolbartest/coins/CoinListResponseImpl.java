package com.example.toolbartest.coins;

import android.content.Context;
import android.util.Log;

import com.example.toolbartest.utils.CacheHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class CoinListResponseImpl implements CoinListResponse {

    private static final String CACHE_NAME = "coin_list_response.json";

    private JSONObject response;

    public CoinListResponseImpl(JSONObject response) {
        this.response = null;

        try {
            if (response.getString("Response").equals("Success")) {
                this.response = response;
            } else {
                Log.d("CoinListResponseImpl", response.toString());
            }
        } catch (JSONException e) {
            Log.d("CoinListResponseImpl", e.getMessage());
        }
    }

    @Override
    public JSONObject getResponse() {
        return response;
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
        }

        return data;
    }

    @Override
    public boolean isSuccess() {
        return response != null;
    }

    @Override
    public boolean saveToFile(Context context) {
        if (response == null) {
            return false;
        }

        CacheHelper.write(context, CACHE_NAME, response.toString());
        return true;
    }

    // @Override
    public static CoinListResponse restoreFromFile(Context context) {
        String text = CacheHelper.read(context, CACHE_NAME);
        JSONObject response = null;

        try {
            response = new JSONObject(text);
        } catch (JSONException e) {
            Log.d("restoreFromFile", e.getMessage());
        }

        if (response == null) {
            return null;
        } else {
            return new CoinListResponseImpl(response);
        }
    }

    // @Override
    public static boolean cacheExists(Context context) {
        // TODO Check timestamp
        return CacheHelper.exists(context, CACHE_NAME);
    }
}
