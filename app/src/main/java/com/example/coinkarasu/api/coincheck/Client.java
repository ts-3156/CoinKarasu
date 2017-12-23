package com.example.coinkarasu.api.coincheck;

import android.content.Context;
import android.util.Log;

import com.example.coinkarasu.api.coincheck.data.Rate;
import com.example.coinkarasu.api.cryptocompare.request.BlockingRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class Client {

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
            Log.e("getSalesRate", e.getMessage() + ", " + response.toString());
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
            Log.e("getTradingRate", e.getMessage() + ", " + response.toString());
        }

        return rate;
    }
}
