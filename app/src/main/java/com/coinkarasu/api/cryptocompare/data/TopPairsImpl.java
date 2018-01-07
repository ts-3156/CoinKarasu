package com.coinkarasu.api.cryptocompare.data;

import android.util.Log;

import com.coinkarasu.api.cryptocompare.response.TopPairsResponse;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

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
    public List<TopPair> getTopPairs() {
        return topPairs;
    }
}
