package com.coinkarasu.api.cryptocompare.response;

import com.coinkarasu.utils.CKLog;

import org.json.JSONException;
import org.json.JSONObject;

public class CoinSnapshotResponseImpl implements CoinSnapshotResponse {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "CoinSnapshotResponseImpl";

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
            CKLog.e(TAG, response.toString(), e);
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
