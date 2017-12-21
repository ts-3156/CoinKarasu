package com.example.coinkarasu.animator;

import android.widget.TextView;

import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.format.SurroundedTrendValueFormat;
import com.example.coinkarasu.format.TrendValueFormat;

public class TrendAnimator extends ValueAnimatorBase {
    private Coin coin;
    private TextView view;
    private TrendValueFormat formatter;

    public TrendAnimator(Coin coin, TextView view) {
        super();
        this.coin = coin;
        this.view = view;
        this.formatter = new SurroundedTrendValueFormat();
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
