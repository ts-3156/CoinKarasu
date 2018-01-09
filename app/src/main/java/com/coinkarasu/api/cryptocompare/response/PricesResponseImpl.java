package com.coinkarasu.api.cryptocompare.response;

import android.content.Context;

import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.DiskCacheHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PricesResponseImpl implements PricesResponse {
    private static final boolean DEBUG = true;
    private static final String TAG = "PricesResponseImpl";

    private JSONObject response;

    private boolean isCache;
    private String[] fromSymbols;
    private String toSymbol;
    private String exchange;

    public PricesResponseImpl(JSONObject response, String[] fromSymbols, String toSymbol, String exchange) {
        this(response, fromSymbols, toSymbol, exchange, false);
    }

    private PricesResponseImpl(JSONObject response, String[] fromSymbols, String toSymbol, String exchange, boolean isCache) {
        this.response = response;
        this.fromSymbols = fromSymbols;
        this.toSymbol = toSymbol;
        this.exchange = exchange;
        this.isCache = isCache;
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
            if(DEBUG) CKLog.e(TAG, response.toString(), e);
        }

        return raw;
    }

    private static String getCacheName(String tag) {
        return "prices_response_" + tag + ".json";
    }

    @Override
    public boolean saveToCache(Context context) {
        return saveToCache(context, "default_tag");
    }

    @Override
    public boolean saveToCache(Context context, String tag) {
        if (response == null) {
            return false;
        }

        JSONObject data = null;
        try {
            data = new JSONObject(response.toString());
            data.put("_fromSymbols", fromSymbols);
            data.put("_toSymbol", toSymbol);
            data.put("_exchange", exchange);
        } catch (JSONException e) {
            if(DEBUG) CKLog.e(TAG, response.toString(), e);
        }

        if (data == null) {
            return false;
        } else {
            DiskCacheHelper.write(context, getCacheName(tag), data.toString());
            return true;
        }
    }

    // @Override
    public static PricesResponse restoreFromCache(Context context, String tag) {
        String text = DiskCacheHelper.read(context, getCacheName(tag));
        JSONObject data;
        ArrayList<String> fromSymbols = new ArrayList<>();
        String toSymbol = null;
        String exchange = null;

        try {
            data = new JSONObject(text);

            JSONArray fromSymbolsArray = data.getJSONArray("_fromSymbols");
            for (int i = 0; i < fromSymbolsArray.length(); i++) {
                fromSymbols.add(fromSymbolsArray.getString(i));
            }
            toSymbol = data.getString("_toSymbol");
            exchange = data.getString("_exchange");

            data.remove("_fromSymbols");
            data.remove("_toSymbol");
            data.remove("_exchange");
        } catch (JSONException e) {
            if(DEBUG) CKLog.e(TAG, e);
            data = null;
        }

        if (data == null) {
            return null;
        } else {
            return new PricesResponseImpl(data, fromSymbols.toArray(new String[fromSymbols.size()]), toSymbol, exchange, true);
        }
    }

    public static boolean isCacheExist(Context context, String tag) {
        return DiskCacheHelper.exists(context, getCacheName(tag));
    }

    @Override
    public boolean isCache() {
        return isCache;
    }

    @Override
    public String getExchange() {
        return exchange;
    }
}
