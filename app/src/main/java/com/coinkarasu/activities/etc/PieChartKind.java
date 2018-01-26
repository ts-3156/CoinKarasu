package com.coinkarasu.activities.etc;

public enum PieChartKind {
    currency("Money flow"),
    exchange("Trading volume");

    public String label;

    PieChartKind(String label) {
        this.label = label;
    }
}
