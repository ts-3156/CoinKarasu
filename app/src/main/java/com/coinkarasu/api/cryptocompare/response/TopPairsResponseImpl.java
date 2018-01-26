package com.coinkarasu.api.cryptocompare.response;

import android.content.Context;
import android.text.TextUtils;

import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.io.CacheFileHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TopPairsResponseImpl implements TopPairsResponse {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "TopPairsResponseImpl";

    private static final long THIRTY_MINUTES = 30 * 60 * 1000;

    private JSONObject response;
    private String fromSymbol;
    private boolean isCache;

    public TopPairsResponseImpl(JSONObject response, String fromSymbol) {
        this(response, fromSymbol, false);
    }

    private TopPairsResponseImpl(JSONObject response, String fromSymbol, boolean isCache) {
        this.response = response;
        this.fromSymbol = fromSymbol;
        this.isCache = isCache;
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
            CKLog.e(TAG, response.toString(), e);
        }

        return data;
    }

    private static String getCacheName(String fromSymbol) {
        return "top_pairs_response_" + fromSymbol + ".json";
    }

    @Override
    public boolean saveToCache(Context context) {
        if (response == null) {
            return false;
        }

        CacheFileHelper.write(context, getCacheName(fromSymbol), response.toString());
        return true;
    }

    public static TopPairsResponse restoreFromCache(Context context, String fromSymbol) {
        String text = CacheFileHelper.read(context, getCacheName(fromSymbol));
        if (TextUtils.isEmpty(text)) {
            if (DEBUG) CKLog.e(TAG, "text is null.");
            return null;
        }
        JSONObject response = null;

        try {
            response = new JSONObject(text);
        } catch (JSONException e) {
            CKLog.e(TAG, text, e);
        }

        if (response == null) {
            return null;
        }

        return new TopPairsResponseImpl(response, fromSymbol, true);
    }

    public static boolean isCacheExist(Context context, String fromSymbol) {
        boolean exists = CacheFileHelper.exists(context, getCacheName(fromSymbol));
        if (!exists) {
            return false;
        }

        return !CacheFileHelper.isExpired(context, getCacheName(fromSymbol), THIRTY_MINUTES);
    }

    @Override
    public boolean isCache() {
        return isCache;
    }

    @Override
    public String getFromSymbol() {
        return fromSymbol;
    }
}
