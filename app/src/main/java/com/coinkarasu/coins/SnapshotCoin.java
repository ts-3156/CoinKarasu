package com.coinkarasu.coins;

import org.json.JSONObject;

public interface SnapshotCoin {
    String getMarket();

    String getFromSymbol();

    String getToSymbol();

    double getPrice();

    double getVolume24Hour();

    long getLastUpdate();

    double getLastVolume();

    double getLastVolumeTo();

    double getVolume24HourTo();

    double getOpen24Hour();

    double getHigh24Hour();

    double getLow24Hour();

    JSONObject toJson();
}
