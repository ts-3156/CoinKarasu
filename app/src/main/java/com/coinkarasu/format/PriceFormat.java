package com.coinkarasu.format;

import android.util.Log;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class PriceFormat {

    private static final boolean DEBUG = true;
    private static final String TAG = "PriceFormat";

    protected String toSymbol;
    protected Currency currency;
    protected NumberFormat formatter;
    private boolean unknownSymbol;

    public PriceFormat(String toSymbol) {
        this.toSymbol = toSymbol;
        this.unknownSymbol = false;

        Locale locale = symbolToLocale(toSymbol);
        this.currency = Currency.getInstance(Currency.getInstance(locale).getCurrencyCode());
        this.formatter = NumberFormat.getCurrencyInstance(locale);
    }

    public String format(String price) {
        return format(Double.valueOf(price));
    }

    public String format(double price) {
        if (toSymbol.equals("BTC")) {
            formatter.setMaximumFractionDigits(6);
            formatter.setMinimumFractionDigits(6);
        } else {
            if (Math.abs(price) >= 1000) {
                formatter.setMaximumFractionDigits(0);
            } else {
                formatter.setMaximumFractionDigits(2);
                formatter.setMinimumFractionDigits(2);
            }
        }

        if (toSymbol.equals("JPY")) {
            price /= Math.pow(10, currency.getDefaultFractionDigits());
        }

        String str = formatter.format(price);
        if (unknownSymbol) {
            if (toSymbol.equals("BTC")) {
                str = str.replace("$", "Ƀ ");
            } else {
                str = str.replace("$", toSymbol + " ");
            }
        } else {
            if (toSymbol.equals("USD")) {
                str = str.replace("$", "$ ");
            }
        }

        return str;
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
            case "BTC":
                locale = Locale.US;
                unknownSymbol = true;
                break;
            case "WEUR":
                locale = Locale.US;
                unknownSymbol = true;
                break;
            case "WUSD":
                locale = Locale.US;
                unknownSymbol = true;
                break;
            default:
                for (Locale l : NumberFormat.getAvailableLocales()) {
                    String code = NumberFormat.getCurrencyInstance(l).getCurrency().getCurrencyCode();
                    if (symbol.equals(code)) {
                        locale = l;
                        break;
                    }
                }
        }

        if (locale == null) {
            locale = Locale.US;
            unknownSymbol = true;
            if (DEBUG) Log.e(TAG, "Invalid symbol " + symbol);
        }

        return locale;
    }
}
