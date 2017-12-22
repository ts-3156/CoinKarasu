package com.example.coinkarasu.api.cryptocompare.data;

import com.example.coinkarasu.coins.AggregatedSnapshotCoin;
import com.example.coinkarasu.coins.SnapshotCoin;

import java.util.ArrayList;

public interface CoinSnapshot {
    String getAlgorithm();

    String getProofOfType();

    long getBlockNumber();

    double getTotalCoinsMined();

    double getBlockReward();

    AggregatedSnapshotCoin getAggregatedSnapshotCoin();

    ArrayList<SnapshotCoin> getSnapshotCoins();

    double getNetHashesPerSecond();
}
