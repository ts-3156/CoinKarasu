package com.example.coinkarasu.api.cryptocompare.data;

import android.util.Log;

import com.example.coinkarasu.coins.PriceMultiFullCoin;
import com.example.coinkarasu.coins.PriceMultiFullCoinImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class ToplistImpl implements Toplist {
    private JSONObject response;
    private ArrayList<PriceMultiFullCoin> coins;

    public ToplistImpl(JSONObject response) {
        this.response = response;
        extract();
    }

    private void extract() {
        coins = new ArrayList<>();

        try {
            JSONObject raw = response.getJSONObject("RAW");
            if (raw == null) {
                Log.e("ToplistImpl#extract", response.toString());
                return;
            }

            Iterator<String> fromSymbols = raw.keys();
            while (fromSymbols.hasNext()) {
                String fromSymbol = fromSymbols.next();
                JSONObject toCoins = raw.getJSONObject(fromSymbol);
                Iterator<String> toSymbols = toCoins.keys();

                while (toSymbols.hasNext()) {
                    String toSymbol = toSymbols.next();
                    JSONObject attrs = toCoins.getJSONObject(toSymbol);
                    coins.add(new PriceMultiFullCoinImpl(attrs));
                }
            }
        } catch (JSONException e) {
            Log.e("ToplistImpl#extract", e.getMessage());
        }
    }

    @Override
    public ArrayList<PriceMultiFullCoin> getCoins() {
        return coins;
    }
}
