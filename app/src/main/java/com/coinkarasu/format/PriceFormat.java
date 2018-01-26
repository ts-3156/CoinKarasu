package com.coinkarasu.format;

import com.coinkarasu.utils.CKLog;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class PriceFormat {
    private static final boolean DEBUG = CKLog.DEBUG;
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

    public CharSequence format(double price) {
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

        return changePrefixIfNecessary(formatter.format(price));
    }

    private String changePrefixIfNecessary(String str) {
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
            if (DEBUG) CKLog.w(TAG, "symbolToLocale() Unknown symbol " + symbol);
        }

        return locale;
    }

    public static PriceFormat getInstance(String toSymbol) {
        if (toSymbol.equals("BTC")) {
            return new WeightedPriceFormat(toSymbol);
        } else {
            return new PriceFormat(toSymbol);
        }
    }
}
