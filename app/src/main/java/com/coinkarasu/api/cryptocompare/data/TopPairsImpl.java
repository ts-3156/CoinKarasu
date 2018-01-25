package com.coinkarasu.api.cryptocompare.data;

import com.coinkarasu.api.cryptocompare.response.TopPairsResponse;
import com.coinkarasu.coins.TopPairCoin;
import com.coinkarasu.coins.TopPairCoinImpl;
import com.coinkarasu.utils.CKLog;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class TopPairsImpl implements TopPairs {
    private static final boolean DEBUG = true;
    private static final String TAG = "TopPairsImpl";

    private List<TopPairCoin> topPairCoins;

    public TopPairsImpl(TopPairsResponse response) {
        if (response == null || response.getData() == null) {
            return;
        }

        JSONArray data = response.getData();
        if (data == null) {
            return;
        }

        topPairCoins = new ArrayList<>(data.length());

        try {
            for (int i = 0; i < data.length(); i++) {
                topPairCoins.add(new TopPairCoinImpl(data.getJSONObject(i)));
            }
        } catch (JSONException e) {
            CKLog.e(TAG, e);
        }
    }

    @Override
    public List<TopPairCoin> getTopPairCoins() {
        return topPairCoins;
    }
}
