package com.coinkarasu.format;

public class SurroundedTrendValueFormat extends TrendValueFormat {
    private static final String DEFAULT_PREFIX = "(";
    private static final String DEFAULT_SUFFIX = ")";

    public SurroundedTrendValueFormat() {
        super();
    }

    @Override
    public CharSequence format(double trend) {
        return DEFAULT_PREFIX + super.format(trend) + DEFAULT_SUFFIX;
    }
}
