package com.example.toolbartest.utils;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class StringHelper {

    public static String join(String delimiter, String[] array) {
        StringBuilder builder = new StringBuilder();
        for (String str : array) {
            if (builder.length() > 0) {
                builder.append(delimiter);
            }
            builder.append(str);
        }

        return builder.toString();
    }

    public static String formatTrend(double trend) {
        NumberFormat formatter = NumberFormat.getPercentInstance();
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);
        return formatter.format(trend);
    }
}
