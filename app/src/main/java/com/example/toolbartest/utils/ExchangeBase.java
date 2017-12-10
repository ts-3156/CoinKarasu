package com.example.toolbartest.utils;

public abstract class ExchangeBase implements Exchange {
    String name;

    public ExchangeBase(String name) {
        this.name = name;
    }

    @Override
    public String toFragmentTag() {
        return "fragment_" + name;
    }

    @Override
    public String[] getFromSymbols() {
        String[] symbols;

        switch (name) {
            case "bitflyer":
                symbols = new String[]{"BTC"};
                break;
            case "coincheck":
                symbols = new String[]{"BTC"};
                break;
            case "zaif":
                symbols = new String[]{"BTC", "XEM", "MONA", "BCH", "ETH"};
                break;
            case "cccagg":
                symbols = new String[]{};
                break;
            default:
                throw new RuntimeException("Invalid exchange " + name);
        }

        return symbols;
    }
}
