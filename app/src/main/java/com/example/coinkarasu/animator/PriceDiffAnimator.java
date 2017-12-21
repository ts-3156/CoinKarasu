package com.example.coinkarasu.animator;

import android.widget.TextView;

import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.format.SignedPriceFormat;

public class PriceDiffAnimator extends PriceAnimator {

    public PriceDiffAnimator(Coin coin, TextView view) {
        super(coin, view);
        this.formatter = new SignedPriceFormat(coin.getToSymbol());
    }

    @Override
    double getPrevValue() {
        return coin.getPrevPriceDiff();
    }

    @Override
    double getValue() {
        return coin.getPriceDiff();
    }

}
