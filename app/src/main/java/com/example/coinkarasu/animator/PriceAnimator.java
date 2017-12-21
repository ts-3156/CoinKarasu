package com.example.coinkarasu.animator;

import android.widget.TextView;

import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.format.PriceFormat;

public class PriceAnimator extends ValueAnimatorBase {
    protected Coin coin;
    protected TextView view;
    protected PriceFormat formatter;

    public PriceAnimator(Coin coin, TextView view) {
        super();
        this.coin = coin;
        this.view = view;
        this.formatter = new PriceFormat(coin.getToSymbol());
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
