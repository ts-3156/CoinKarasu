package com.example.coinkarasu.coins;

public interface AggregatedSnapshotCoin extends SnapshotCoin {
    double getVolumeDay();

    double getVolumeDayTo();

    double getOpenDay();

    double getHighDay();

    double getLowDay();

    String getLastMarket();
}
