package com.coinkarasu.api.cryptocompare.data;

import com.coinkarasu.api.cryptocompare.response.PricesResponse;
import com.coinkarasu.coins.PriceMultiFullCoin;
import com.coinkarasu.coins.PriceMultiFullCoinImpl;
import com.coinkarasu.utils.CKLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PricesImpl implements Prices {
    private static final boolean DEBUG = true;
    private static final String TAG = "PricesImpl";

    private String exchange;

    private List<PriceMultiFullCoin> coins;

    public PricesImpl(PricesResponse response) {
        this.exchange = response.getExchange();
        this.coins = new ArrayList<>();
        extract(response);
    }

    private void extract(PricesResponse response) {
        JSONObject raw = response.getRaw();
        if (raw == null) {
            if (DEBUG) CKLog.e(TAG, "extract() " + response.toString());
            return;
        }

        try {
            for (Iterator<String> it = raw.keys(); it.hasNext(); ) {
                String fromSymbol = it.next();
                JSONObject values = raw.getJSONObject(fromSymbol);

                String toSymbol = values.keys().next();
                JSONObject attrs = raw.getJSONObject(fromSymbol).getJSONObject(toSymbol);

                coins.add(new PriceMultiFullCoinImpl(attrs));
            }
        } catch (JSONException e) {
            CKLog.e(TAG, e);
        }
    }

    @Override
    public List<PriceMultiFullCoin> getCoins() {
        return coins;
    }

    @Override
    public String getExchange() {
        return exchange;
    }
}
