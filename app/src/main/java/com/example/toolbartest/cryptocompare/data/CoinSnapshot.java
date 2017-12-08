package com.example.toolbartest.cryptocompare.data;

import com.example.toolbartest.coins.AggregatedData;

import java.util.ArrayList;

public interface CoinSnapshot {
    String getAlgorithm();

    String getProofOfType();

    long getBlockNumber();

    double getTotalCoinsMined();

    double getBlockReward();

    AggregatedData getAggregatedData();

    ArrayList<Exchange> getExchanges();

    double getNetHashesPerSecond();
}
