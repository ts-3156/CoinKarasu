package com.coinkarasu.animator;

import android.graphics.drawable.Drawable;
import android.view.View;

public class PriceBgColorAnimator extends ColorAnimatorBase {

    private int prevColor;
    private int color;
    private Drawable prevBackground;
    private View view;

    public PriceBgColorAnimator(int fromColor, int toColor, View view) {
        super();
        this.prevColor = fromColor;
        this.color = toColor;
//        this.prevBackground = view.getBackground().getConstantState().newDrawable().mutate();
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

    @Override
    void onAnimationEnd() {
//        view.setBackground(prevBackground);
    }
}
