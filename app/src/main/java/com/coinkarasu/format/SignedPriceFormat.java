package com.coinkarasu.format;

public class SignedPriceFormat extends PriceFormat {

    public SignedPriceFormat(String toSymbol) {
        super(toSymbol);
    }

    @Override
    public String format(double price) {
        String str = super.format(price);
        if (price > 0.0) {
            str = "+" + str;
        }

        return str;
    }
}
