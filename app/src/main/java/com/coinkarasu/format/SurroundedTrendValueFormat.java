package com.coinkarasu.format;

public class SurroundedTrendValueFormat extends TrendValueFormat {
    private String prefix;
    private String suffix;

    public SurroundedTrendValueFormat() {
        super();
        prefix = "(";
        suffix = ")";
    }

    @Override
    public String format(double trend) {
        String str = super.format(trend);
        return prefix + str + suffix;
    }
}
