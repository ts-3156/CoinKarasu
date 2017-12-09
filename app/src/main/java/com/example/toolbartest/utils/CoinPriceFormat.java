package com.example.toolbartest.utils;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class CoinPriceFormat {
    private String toSymbol;

    public CoinPriceFormat(String toSymbol) {
        this.toSymbol = toSymbol;
    }

    public String format(String price) {
        return format(Double.valueOf(price));
    }

    public String format(double price) {
        Locale locale = symbolToLocale(toSymbol);
        Currency currency = Currency.getInstance(Currency.getInstance(locale).getCurrencyCode());
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        String value;

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

    private Locale symbolToLocale(String symbol) {
        Locale locale = null;

        switch (symbol) {
            case "JPY":
                locale = Locale.JAPAN;
                break;
            case "USD":
                locale = Locale.US;
                break;
            default:
                locale = Locale.JAPAN;
        }

        return locale;
    }
}
