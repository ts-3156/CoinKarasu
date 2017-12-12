package com.example.coinkarasu.utils;

public class ExchangeImpl extends ExchangeBase {
    public ExchangeImpl(String name) {
        super(name);
    }

    public static String exchangeToDisplayName(String exchange) {
        String name;

        switch (exchange) {
            case "bitflyer":
                name = "BitFlyer";
                break;
            case "coincheck":
                name = "Coincheck";
                break;
            case "zaif":
                name = "Zaif";
                break;
            case "cccagg":
                name = "Aggregated Index";
                break;
            default:
                throw new RuntimeException("Invalid exchange " + exchange);
        }

        return name;
    }
}
