package com.coinkarasu.adapters;

import com.robinhood.spark.SparkAdapter;

public class CoinListSparkAdapter extends SparkAdapter {
    private double[] yData;

    public CoinListSparkAdapter(double[] yData) {
        this.yData = yData;
    }

    @Override
    public int getCount() {
        return yData.length;
    }

    @Override
    public Object getItem(int index) {
        return yData[index];
    }

    @Override
    public float getY(int index) {
        return (float) yData[index];
    }
}
