package com.coinkarasu.animator;

import android.widget.TextView;

import com.coinkarasu.coins.Coin;
import com.coinkarasu.format.PriceFormat;
import com.coinkarasu.format.WeightedPriceFormat;

public class PriceAnimator extends ValueAnimatorBase {
    protected Coin coin;
    protected TextView view;
    protected PriceFormat formatter;

    public PriceAnimator(Coin coin, TextView view) {
        super();
        this.coin = coin;
        this.view = view;
        if (coin.getToSymbol().equals("BTC")) {
            this.formatter = new WeightedPriceFormat(coin.getToSymbol());
        } else {
            this.formatter = new PriceFormat(coin.getToSymbol());
        }
    }

    @Override
    double getPrevValue() {
        return coin.getPrevPrice();
    }

    @Override
    double getValue() {
        return coin.getPrice();
    }

    @Override
    void setValue(double value) {
        view.setText(formatter.format(value));
    }
}
