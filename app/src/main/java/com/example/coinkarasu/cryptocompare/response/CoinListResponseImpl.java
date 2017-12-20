package com.example.coinkarasu.cryptocompare.response;

import android.content.Context;
import android.util.Log;

import com.example.coinkarasu.utils.CacheHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

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
    public boolean saveToCache(Context context) {
        if (response == null) {
            return false;
        }

        CacheHelper.write(context, CACHE_NAME, response.toString());
        return true;
    }

    // @Override
    public static CoinListResponse restoreFromCache(Context context) {
        String text = CacheHelper.read(context, CACHE_NAME);
        JSONObject response = null;

        try {
            response = new JSONObject(text);
        } catch (JSONException e) {
            Log.e("restoreFromCache", e.getMessage());
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

    // @Override
    public static Date lastModified(Context context) {
        return CacheHelper.lastModified(context, CACHE_NAME);
    }
}
