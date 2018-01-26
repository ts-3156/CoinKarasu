package com.coinkarasu.api.cryptocompare.response;

import com.coinkarasu.utils.CKLog;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class CoinListResponse {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "CoinListResponse";

    public abstract JSONObject getData();

    abstract boolean isSuccess();

    public static CoinListResponse buildBy(String str) {
        try {
            return new CoinListResponseImpl(new JSONObject(str));
        } catch (JSONException e) {
            CKLog.e(TAG, str == null ? "null" : str, e);
            return null;
        }
    }
}
