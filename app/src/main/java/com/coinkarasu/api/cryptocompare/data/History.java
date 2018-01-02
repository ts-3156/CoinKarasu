package com.coinkarasu.api.cryptocompare.data;

public interface History {
    long getTime();

    double getClose();

    double getHigh();

    double getLow();

    double getOpen();

    double getVolumeFrom();

    double getVolumeTo();

    String getFromSymbol();

    String getToSymbol();

    String toString();
}
