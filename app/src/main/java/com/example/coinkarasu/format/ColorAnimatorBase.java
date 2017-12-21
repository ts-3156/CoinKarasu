package com.example.coinkarasu.format;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;

public abstract class ColorAnimatorBase implements ValueAnimator.AnimatorUpdateListener {
    public static final long DURATION = 1000;

    private boolean isStarted;
    private ValueAnimator animator;

    ColorAnimatorBase() {
        this.isStarted = false;
    }

    public void start() {
        if (isStarted) {
            return;
        }

        animator = ValueAnimator.ofObject(new ArgbEvaluator(), getPrevValue(), getValue());
        animator.setDuration(DURATION);
        animator.addUpdateListener(this);
        animator.start();
        isStarted = true;
    }

    public void cancel() {
        animator.cancel();
    }

    int getPrevValue() {
        throw new RuntimeException("Stub");
    }

    int getValue() {
        throw new RuntimeException("Stub");
    }

    void setValue(int value) {
        throw new RuntimeException("Stub");
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animator) {
        setValue((int) animator.getAnimatedValue());
    }
}