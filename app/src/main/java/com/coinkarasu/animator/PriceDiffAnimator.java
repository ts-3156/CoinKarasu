package com.coinkarasu.animator;

import android.widget.TextView;

import com.coinkarasu.coins.Coin;
import com.coinkarasu.format.SignedPriceFormat;

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
