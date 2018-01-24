package com.coinkarasu.format;

public class SignedPriceFormat extends PriceFormat {

    public SignedPriceFormat(String toSymbol) {
        super(toSymbol);
    }

    @Override
    public CharSequence format(double price) {
        CharSequence seq = super.format(price);
        String str = seq.toString();
        if (price > 0.0) {
            str = "+" + str;
        }

        return str;
    }
}
