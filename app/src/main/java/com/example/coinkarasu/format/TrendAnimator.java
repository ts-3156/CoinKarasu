package com.example.coinkarasu.format;

import android.widget.TextView;

import com.example.coinkarasu.coins.Coin;

public class TrendAnimator extends ValueAnimatorBase {
    private Coin coin;
    private TextView view;
    private TrendValueFormat formatter;

    public TrendAnimator(Coin coin, TextView view) {
        super();
        this.coin = coin;
        this.view = view;
        this.formatter = new TrendValueFormat();
    }

    @Override
    double getPrevValue() {
        return coin.getPrevTrend();
    }

    @Override
    double getValue() {
        return coin.getTrend();
    }

    @Override
    void setValue(double value) {
        view.setText(formatter.format(value));
    }
}
