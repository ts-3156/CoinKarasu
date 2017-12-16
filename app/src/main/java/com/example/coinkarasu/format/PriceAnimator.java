package com.example.coinkarasu.format;

import android.widget.TextView;

import com.example.coinkarasu.coins.Coin;

public class PriceAnimator extends ValueAnimatorBase {
    private Coin coin;
    private TextView view;
    private PriceFormat formatter;

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
