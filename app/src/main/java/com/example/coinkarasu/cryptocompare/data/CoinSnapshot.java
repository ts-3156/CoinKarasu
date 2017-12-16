package com.example.coinkarasu.cryptocompare.data;

import com.example.coinkarasu.coins.AggregatedData;
import com.example.coinkarasu.coins.SnapshotCoin;

import java.util.ArrayList;

public interface CoinSnapshot {
    String getAlgorithm();

    String getProofOfType();

    long getBlockNumber();

    double getTotalCoinsMined();

    double getBlockReward();

    AggregatedData getAggregatedData();

    ArrayList<SnapshotCoin> getSnapshotCoins();

    double getNetHashesPerSecond();
}
