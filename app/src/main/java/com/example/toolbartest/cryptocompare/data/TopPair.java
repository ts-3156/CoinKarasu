package com.example.toolbartest.cryptocompare.data;

import java.util.ArrayList;

public interface TopPair {

    String getExchange();

    String getFromSymbol();

    String getToSymbol();

    double getVolume24h();

    double getVolume24hTo();
}
