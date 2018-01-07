package com.coinkarasu.api.cryptocompare.data;

import com.coinkarasu.coins.AggregatedSnapshotCoin;
import com.coinkarasu.coins.SnapshotCoin;

import java.util.List;

public interface CoinSnapshot {
    String getAlgorithm();

    String getProofOfType();

    long getBlockNumber();

    double getTotalCoinsMined();

    double getBlockReward();

    AggregatedSnapshotCoin getAggregatedSnapshotCoin();

    List<SnapshotCoin> getSnapshotCoins();

    double getNetHashesPerSecond();
}
