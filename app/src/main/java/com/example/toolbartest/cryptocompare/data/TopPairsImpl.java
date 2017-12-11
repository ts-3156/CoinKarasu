package com.example.toolbartest.cryptocompare.data;

import android.util.Log;

import com.example.toolbartest.cryptocompare.response.TopPairsResponse;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class TopPairsImpl implements TopPairs {

    ArrayList<TopPair> topPairs;

    public TopPairsImpl(TopPairsResponse response) {
        if (response == null || response.getData() == null) {
            return;
        }

        JSONArray data = response.getData();
        if (data == null) {
            topPairs = new ArrayList<>();
            return;
        }

        topPairs = new ArrayList<>(data.length());

        try {
            for (int i = 0; i < data.length(); i++) {
                topPairs.add(new TopPairImpl(data.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.d("TopPairsImpl", e.getMessage());
        }

    }

    @Override
    public ArrayList<TopPair> getTopPairs() {
        return topPairs;
    }
}
