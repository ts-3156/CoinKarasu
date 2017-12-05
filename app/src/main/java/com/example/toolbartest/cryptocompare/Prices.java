package com.example.toolbartest.cryptocompare;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class Prices {
    private HashMap<String, Double> prices;
    private HashMap<String, Double> trends;

    private Prices(HashMap<String, Double> prices, HashMap<String, Double> trends) {
        this.prices = prices;
        this.trends = trends;
    }

    public static Prices buildByResponse(JSONObject response) {
        HashMap<String, Double> prices = new HashMap<>();
        HashMap<String, Double> trends = new HashMap<>();

        try {
            JSONObject raw = response.getJSONObject("RAW");

            for (Iterator<String> it = raw.keys(); it.hasNext(); ) {
                String fromSymbol = it.next();
                JSONObject values = raw.getJSONObject(fromSymbol);

                String toSymbol = values.keys().next();
                JSONObject attrs = raw.getJSONObject(fromSymbol).getJSONObject(toSymbol);

                prices.put(fromSymbol, attrs.getDouble("PRICE"));
                trends.put(fromSymbol, attrs.getDouble("CHANGEPCT24HOUR") / 100.0);

            }
        } catch (JSONException e) {
        }

        return new Prices(prices, trends);
    }

    public HashMap<String, Double> getPrices() {
        return prices;
    }

    public HashMap<String, Double> getTrends() {
        return trends;
    }
}
