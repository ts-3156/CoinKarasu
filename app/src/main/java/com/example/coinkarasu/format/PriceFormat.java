package com.example.coinkarasu.format;

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
        if (toSymbol.equals("BTC")) {
            str = str.replace("$", "Ƀ");
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
                break;
            default:
                for (Locale l : NumberFormat.getAvailableLocales()) {
                    String code = NumberFormat.getCurrencyInstance(l).getCurrency().getCurrencyCode();
                    if (symbol.equals(code)) {
                        locale = l;
                        break;
                    }
                }
                if (locale == null) {
                    throw new RuntimeException("Invalid symbol " + symbol);
                }
        }

        return locale;
    }
}
