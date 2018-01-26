package com.coinkarasu.api.cryptocompare.response;

import com.coinkarasu.utils.CKLog;

import org.json.JSONException;
import org.json.JSONObject;

public class CoinListResponseImpl extends CoinListResponse {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "CoinListResponseImpl";

    private JSONObject response;

    public CoinListResponseImpl(JSONObject response) {
        this.response = response;
    }

    @Override
    public JSONObject getData() {
        if (!isSuccess()) {
            return null;
        }

        JSONObject data = null;

        try {
            data = response.getJSONObject("Data");
        } catch (JSONException e) {
            CKLog.e(TAG, response.toString(), e);
        }

        return data;
    }

    @Override
    public boolean isSuccess() {
        try {
            return response != null && response.has("Response")
                    && response.getString("Response").equals("Success");
        } catch (JSONException e) {
            CKLog.e(TAG, response == null ? "null" : response.toString(), e);
            return false;
        }
    }

    @Override
    public String toString() {
        return response.toString();
    }
}
