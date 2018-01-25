package com.coinkarasu.api.cryptocompare.data;

import com.coinkarasu.api.cryptocompare.response.CoinSnapshotResponse;
import com.coinkarasu.coins.AggregatedSnapshotCoin;
import com.coinkarasu.coins.AggregatedSnapshotCoinImpl;
import com.coinkarasu.coins.SnapshotCoin;
import com.coinkarasu.coins.SnapshotCoinImpl;
import com.coinkarasu.utils.CKLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CoinSnapshotImpl implements CoinSnapshot {
    private static final boolean DEBUG = true;
    private static final String TAG = "CoinSnapshotImpl";

    private String algorithm;
    private String proofOfType;
    private long blockNumber;
    private double netHashesPerSecond;
    private double totalCoinsMined;
    private double blockReward;
    private AggregatedSnapshotCoin aggregatedSnapshotCoin;
    private List<SnapshotCoin> snapshotCoins;

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
                aggregatedSnapshotCoin = new AggregatedSnapshotCoinImpl(data.getJSONObject("AggregatedData"));

            if (data.has("Exchanges")) {
                JSONArray exchanges = data.getJSONArray("Exchanges");

                for (int i = 0; i < exchanges.length(); i++) {
                    this.snapshotCoins.add(new SnapshotCoinImpl(exchanges.getJSONObject(i)));
                }
            }
        } catch (JSONException e) {
            CKLog.e(TAG, response.toString(), e);
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
    public AggregatedSnapshotCoin getAggregatedSnapshotCoin() {
        return aggregatedSnapshotCoin;
    }

    @Override
    public List<SnapshotCoin> getSnapshotCoins() {
        return snapshotCoins;
    }

    @Override
    public double getNetHashesPerSecond() {
        return netHashesPerSecond;
    }
}
