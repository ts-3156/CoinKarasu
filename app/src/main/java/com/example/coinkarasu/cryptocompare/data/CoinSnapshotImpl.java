package com.example.coinkarasu.cryptocompare.data;

import android.util.Log;

import com.example.coinkarasu.coins.AggregatedDataImpl;
import com.example.coinkarasu.coins.SnapshotCoin;
import com.example.coinkarasu.coins.SnapshotCoinImpl;
import com.example.coinkarasu.cryptocompare.response.CoinSnapshotResponse;
import com.example.coinkarasu.coins.AggregatedData;

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
    private ArrayList<SnapshotCoin> snapshotCoins;

    public CoinSnapshotImpl(CoinSnapshotResponse response) {
        if (response == null || response.getData() == null) {
            return;
        }

        JSONObject data = response.getData();
        if (data == null) {
            return;
        }

        snapshotCoins = new ArrayList<>();

        try {
            if (data.has("Algorithm"))
                algorithm = data.getString("Algorithm");
            if (data.has("ProofType"))
                proofOfType = data.getString("ProofType");
            if (data.has("BlockNumber"))
                blockNumber = data.getLong("BlockNumber");
            if (data.has("NetHashesPerSecond"))
                netHashesPerSecond = data.getDouble("NetHashesPerSecond");
            if (data.has("TotalCoinsMined"))
                totalCoinsMined = data.getDouble("TotalCoinsMined");
            if (data.has("BlockReward"))
                blockReward = data.getDouble("BlockReward");

            if (data.has("AggregatedData"))
                aggregatedData = new AggregatedDataImpl(data.getJSONObject("AggregatedData"));

            if (data.has("Exchanges")) {
                JSONArray exchanges = data.getJSONArray("Exchanges");

                for (int i = 0; i < exchanges.length(); i++) {
                    this.snapshotCoins.add(new SnapshotCoinImpl(exchanges.getJSONObject(i)));
                }
            }
        } catch (JSONException e) {
            Log.e("CoinSnapshotImpl", e.getMessage());
            Log.e("CoinSnapshotImpl", response.toString());
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
    public ArrayList<SnapshotCoin> getSnapshotCoins() {
        return snapshotCoins;
    }

    @Override
    public double getNetHashesPerSecond() {
        return netHashesPerSecond;
    }
}
