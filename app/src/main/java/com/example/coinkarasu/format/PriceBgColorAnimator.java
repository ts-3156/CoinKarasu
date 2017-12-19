package com.example.coinkarasu.format;

import android.view.View;

public class PriceBgColorAnimator extends ColorAnimatorBase {
    private int prevColor;
    private int color;
    private View view;

    public PriceBgColorAnimator(int fromColor, int toColor, View view) {
        super();
        this.prevColor = fromColor;
        this.color = toColor;
        this.view = view;
    }

    @Override
    int getPrevValue() {
        return prevColor;
    }

    @Override
    int getValue() {
        return color;
    }

    @Override
    void setValue(int value) {
        view.setBackgroundColor(value);
    }
}
