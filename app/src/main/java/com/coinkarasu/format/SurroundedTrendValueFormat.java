package com.coinkarasu.format;

public class SurroundedTrendValueFormat extends TrendValueFormat {
    private static final String DEFAULT_PREFIX = "(";
    private static final String DEFAULT_SUFFIX = ")";

    public SurroundedTrendValueFormat() {
        super();
    }

    @Override
    public String format(double trend) {
        String str = super.format(trend);
        return DEFAULT_PREFIX + str + DEFAULT_SUFFIX;
    }
}
