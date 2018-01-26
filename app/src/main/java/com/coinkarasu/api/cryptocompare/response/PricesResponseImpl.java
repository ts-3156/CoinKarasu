package com.coinkarasu.api.cryptocompare.response;

import com.coinkarasu.utils.CKLog;

import org.json.JSONException;
import org.json.JSONObject;

public class PricesResponseImpl implements PricesResponse {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "PricesResponseImpl";

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
            CKLog.e(TAG, response.toString(), e);
        }

        return raw;
    }
}
