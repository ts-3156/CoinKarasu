package com.coinkarasu.format;

import java.text.NumberFormat;

public class TrendValueFormat {
    private NumberFormat formatter;

    public TrendValueFormat() {
        this.formatter = NumberFormat.getPercentInstance();
    }

    public String format(double trend) {
        if (Math.abs(trend) > 1.0) {
            formatter.setMaximumFractionDigits(0);
            formatter.setMinimumFractionDigits(0);
        } else {
            formatter.setMaximumFractionDigits(2);
            formatter.setMinimumFractionDigits(2);
        }

        return formatter.format(trend);
    }
}
