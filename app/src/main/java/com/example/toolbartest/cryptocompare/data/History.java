package com.example.toolbartest.cryptocompare.data;

public interface History {
    long getTime();

    void setTime(long time);

    double getClose();

    void setClose(double close);

    double getHigh();

    void setHigh(double high);

    double getLow();

    void setLow(double low);

    double getOpen();

    void setOpen(double open);

    double getVolumeFrom();

    void setVolumeFrom(double volumeFrom);

    double getVolumeTo();

    void setVolumeTo(double volumeTo);

    String getFromSymbol();

    void setFromSymbol(String fromSymbol);

    String getToSymbol();

    void setToSymbol(String toSymbol);

    String toString();
}
