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

    public static String formatPrice(double price, String toSymbol) {
        Locale locale = LocaleHelper.symbolToLocale(toSymbol);
        Currency currency = Currency.getInstance(Currency.getInstance(locale).getCurrencyCode());
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        String value = "";

        if (toSymbol.equals("JPY")) {
            if (price > 1000.0) {
                formatter.setMaximumFractionDigits(0);
                formatter.setMinimumFractionDigits(0);
            } else {
                formatter.setMaximumFractionDigits(2);
                formatter.setMinimumFractionDigits(2);
            }
            value = formatter.format(price / Math.pow(10, currency.getDefaultFractionDigits()));
        } else {
            value = formatter.format(price);
        }

        return value;
    }

    public static String formatTrend(double trend) {
        NumberFormat formatter = NumberFormat.getPercentInstance();
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);
        return formatter.format(trend);
    }
}
