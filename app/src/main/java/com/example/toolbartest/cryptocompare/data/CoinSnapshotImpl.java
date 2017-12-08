package com.example.toolbartest.cryptocompare.data;

import android.util.Log;

import com.example.toolbartest.coins.AggregatedData;
import com.example.toolbartest.coins.AggregatedDataImpl;
import com.example.toolbartest.cryptocompare.response.CoinSnapshotResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CoinSnapshotImpl implements CoinSnapshot {
    private String algorithm;
    private String proofOfType;
    private long blockNumber;
    private double netHashesPerSecond;
    private double totalCoinsMined;
    private double blockReward;
    private AggregatedData aggregatedData;
    private ArrayList<Exchange> exchanges;

    public CoinSnapshotImpl(CoinSnapshotResponse response) {
        if (response == null || response.getData() == null) {
            return;
        }

        JSONObject data = response.getData();
        exchanges = new ArrayList<>();

        try {
            algorithm = data.getString("Algorithm");
            proofOfType = data.getString("ProofType");
            blockNumber = data.getLong("BlockNumber");
            netHashesPerSecond = data.getDouble("NetHashesPerSecond");
            totalCoinsMined = data.getDouble("TotalCoinsMined");
            blockReward = data.getDouble("BlockReward");

            aggregatedData = new AggregatedDataImpl(data.getJSONObject("AggregatedData"));

            JSONArray exchanges = data.getJSONArray("Exchanges");

            for (int i = 0; i < exchanges.length(); i++) {
                this.exchanges.add(new ExchangeImpl(exchanges.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.d("CoinSnapshotImpl", e.getMessage());
        }

    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public String getProofOfType() {
        return proofOfType;
    }

    @Override
    public long getBlockNumber() {
        return blockNumber;
    }

    @Override
    public double getTotalCoinsMined() {
        return totalCoinsMined;
    }

    @Override
    public double getBlockReward() {
        return blockReward;
    }

    @Override
    public AggregatedData getAggregatedData() {
        return aggregatedData;
    }

    @Override
    public ArrayList<Exchange> getExchanges() {
        return exchanges;
    }

    @Override
    public double getNetHashesPerSecond() {
        return netHashesPerSecond;
    }
}
