package com.example.coinkarasu.format;

import android.util.Log;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class PriceTabFormat {
    private String toSymbol;

    public PriceTabFormat(String toSymbol) {
        this.toSymbol = toSymbol;
    }

    public String format(double price) {
        Locale locale = symbolToLocale(toSymbol);
        Currency currency = Currency.getInstance(Currency.getInstance(locale).getCurrencyCode());
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);

        if (Math.abs(price) >= 1000) {
            formatter.setMaximumFractionDigits(0);
        } else {
            formatter.setMaximumFractionDigits(3);
            formatter.setMinimumFractionDigits(2);
        }

        if (toSymbol.equals("JPY")) {
            price /= Math.pow(10, currency.getDefaultFractionDigits());
        }

        return formatter.format(price);
    }

    private Locale symbolToLocale(String symbol) {
        Locale locale;

        switch (symbol) {
            case "JPY":
                locale = Locale.JAPAN;
                break;
            case "USD":
                locale = Locale.US;
                break;
            default:
                Log.d("Invalid locale", symbol);
                locale = Locale.JAPAN;
        }

        return locale;
    }
}
