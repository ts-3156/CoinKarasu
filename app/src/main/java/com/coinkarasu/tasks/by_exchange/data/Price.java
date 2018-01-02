package com.coinkarasu.tasks.by_exchange.data;

import android.util.Log;

import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;

import org.json.JSONException;
import org.json.JSONObject;

public class Price {

    private static final boolean DEBUG = true;
    private static final String TAG = "Price";

    Exchange exchange;
    CoinKind coinKind;

    public String fromSymbol;
    public String toSymbol;
    public double price;
    public double priceDiff;
    public double trend;

    public Price(Exchange exchange, CoinKind coinKind, String fromSymbol, String toSymbol, double price) {
        this.exchange = exchange;
        this.coinKind = coinKind;
        this.fromSymbol = fromSymbol;
        this.toSymbol = toSymbol;
        this.price = price;
        this.priceDiff = 0.0;
        this.trend = 0.0;
    }

    public static Price buildByJson(JSONObject data) {
        Price price = null;

        try {
            Exchange exchange = Exchange.valueOf(data.getString("exchange"));
            CoinKind coinKind = CoinKind.valueOf(data.getString("coinKind"));
            String fromSymbol = data.getString("fromSymbol");
            String toSymbol = data.getString("toSymbol");
            double _price = data.getDouble("price");
            double priceDiff = data.getDouble("priceDiff");
            double trend = data.getDouble("trend");

            price = new Price(exchange, coinKind, fromSymbol, toSymbol, priceDiff);
            price.price = _price;
            price.priceDiff = priceDiff;
            price.trend = trend;
        } catch (JSONException e) {
            if (DEBUG) Log.e(TAG, e.getMessage());
        }

        return price;
    }

    public JSONObject toJson() {
        JSONObject data = new JSONObject();

        try {
            data.put("exchange", exchange.name());
            data.put("coinKind", coinKind.name());
            data.put("fromSymbol", fromSymbol);
            data.put("toSymbol", toSymbol);
            data.put("price", price);
            data.put("priceDiff", priceDiff);
            data.put("trend", trend);
        } catch (JSONException e) {
            if (DEBUG) Log.e(TAG, e.getMessage());
        }

        return data;
    }
}
