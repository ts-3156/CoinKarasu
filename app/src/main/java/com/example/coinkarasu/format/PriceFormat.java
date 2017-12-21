package com.example.coinkarasu.format;

import android.util.Log;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class PriceFormat {
    protected String toSymbol;
    protected Currency currency;
    protected NumberFormat formatter;

    public PriceFormat(String toSymbol) {
        this.toSymbol = toSymbol;

        Locale locale = symbolToLocale(toSymbol);
        this.currency = Currency.getInstance(Currency.getInstance(locale).getCurrencyCode());
        this.formatter = NumberFormat.getCurrencyInstance(locale);
    }

    public String format(String price) {
        return format(Double.valueOf(price));
    }

    public String format(double price) {
        if (Math.abs(price) >= 1000) {
            formatter.setMaximumFractionDigits(0);
        } else {
            formatter.setMaximumFractionDigits(2);
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
