package com.coinkarasu.api.coincheck;

import android.content.Context;

import com.coinkarasu.api.coincheck.data.Rate;
import com.coinkarasu.api.cryptocompare.request.BlockingRequest;
import com.coinkarasu.utils.CKLog;

import org.json.JSONException;
import org.json.JSONObject;

public class Client {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "Client";

    private Context context;

    public Client(Context context) {
        this.context = context;
    }

    public Rate getSalesRate(String fromSymbol, String toSymbol) {
        String url = "https://coincheck.com/api/rate/" + fromSymbol.toLowerCase() + "_" + toSymbol.toLowerCase();
        JSONObject response = new BlockingRequest(context, url).perform();

        if (response == null) {
            return null;
        }

        Rate rate = new Rate(fromSymbol, toSymbol);
        try {
            rate.value = response.getDouble("rate");
        } catch (JSONException e) {
            CKLog.e(TAG, response.toString(), e);
        }

        return rate;
    }

    public Rate getTradingRate(String orderType) {
        String url = "https://coincheck.com/api/exchange/orders/rate?pair=btc_jpy&amount=1" +
                "&order_type=" + orderType;
        JSONObject response = new BlockingRequest(context, url).perform();

        if (response == null) {
            return null;
        }

        Rate rate = new Rate("BTC", "JPY");
        try {
            rate.value = response.getDouble("rate");
        } catch (JSONException e) {
            CKLog.e(TAG, response.toString(), e);
        }

        return rate;
    }
}
