package com.example.coinkarasu.coins;

import org.json.JSONObject;

public interface PriceMultiFullCoin {
    String getMarket();

    String getFromSymbol();

    String getToSymbol();

    double getPrice();

    long geLastUpdate();

    double getLastVolume();

    double getLastVolumeTo();

    double getVolumeDay();

    double getVolumeDayTo();

    double getVolume24Hour();

    double getVolume24HourTo();

    double getOpenDay();

    double getHighDay();

    double getLowDay();

    double getOpen24Hour();

    double getHigh24Hour();

    double getLow24Hour();

    String getLastMarket();

    double getChange24Hour();

    double getChangePct24Hour();

    double getChangeDay();

    double getChangePctDay();

    double getSupply();

    double getMktCap();

    double getTotalVolume24h();

    double getTotalVolume24hTo();

    JSONObject toJson();
}
