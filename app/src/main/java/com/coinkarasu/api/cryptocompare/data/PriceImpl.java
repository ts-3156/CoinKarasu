package com.coinkarasu.api.cryptocompare.data;

import com.coinkarasu.api.cryptocompare.response.PricesResponse;
import com.coinkarasu.coins.PriceMultiFullCoin;
import com.coinkarasu.coins.PriceMultiFullCoinImpl;
import com.coinkarasu.utils.CKLog;

import org.json.JSONException;
import org.json.JSONObject;

public class PriceImpl implements Price {
    private static final boolean DEBUG = true;
    private static final String TAG = "PriceImpl";

    private PriceMultiFullCoin coin;
    private String exchange;

    public PriceImpl(PricesResponse response) {
        exchange = response.getExchange();

        JSONObject raw = response.getRaw();
        if (raw == null) {
            if (DEBUG) CKLog.e(TAG, "PriceImpl() " + response.toString());
            return;
        }

        try {
            String fromSymbol = raw.keys().next();
            JSONObject values = raw.getJSONObject(fromSymbol);

            String toSymbol = values.keys().next();
            JSONObject attrs = values.getJSONObject(toSymbol);

            coin = new PriceMultiFullCoinImpl(attrs);
        } catch (JSONException e) {
            if (DEBUG) CKLog.e(TAG, response.toString(), e);
        }
    }

    @Override
    public PriceMultiFullCoin getCoin() {
        return coin;
    }

    @Override
    public String getExchange() {
        return exchange;
    }
}
