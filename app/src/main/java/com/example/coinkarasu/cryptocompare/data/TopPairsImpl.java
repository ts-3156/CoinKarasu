package com.example.coinkarasu.cryptocompare.data;

import android.util.Log;

import com.example.coinkarasu.cryptocompare.response.TopPairsResponse;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class TopPairsImpl implements TopPairs {

    private ArrayList<TopPair> topPairs;

    public TopPairsImpl(TopPairsResponse response) {
        if (response == null || response.getData() == null) {
            return;
        }

        JSONArray data = response.getData();
        if (data == null) {
            return;
        }

        topPairs = new ArrayList<>(data.length());

        try {
            for (int i = 0; i < data.length(); i++) {
                topPairs.add(new TopPairImpl(data.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e("TopPairsImpl", e.getMessage());
        }
    }

    @Override
    public ArrayList<TopPair> getTopPairs() {
        return topPairs;
    }
}
