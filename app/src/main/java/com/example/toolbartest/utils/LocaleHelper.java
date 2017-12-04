package com.example.toolbartest.utils;

import java.util.Locale;

public class LocaleHelper {
    public static Locale symbolToLocale(String symbol) {
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
