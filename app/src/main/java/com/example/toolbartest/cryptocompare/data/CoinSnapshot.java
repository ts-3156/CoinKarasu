package com.example.toolbartest.cryptocompare.data;

import java.util.ArrayList;

public interface CoinSnapshot {
    String getAlgorithm();

    String getProofOfType();

    long getBlockNumber();

    double getTotalCoinsMined();

    double getBlockReward();

    Object getAggregatedData();

    ArrayList<Exchange> getExchanges();

    double getNetHashesPerSecond();
}
