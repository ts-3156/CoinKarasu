package com.example.coinkarasu.format;

import android.animation.ValueAnimator;

public abstract class ValueAnimatorBase implements ValueAnimator.AnimatorUpdateListener {
    public static final long DURATION = 1000;

    private boolean isStarted;
    private ValueAnimator animator;

    ValueAnimatorBase() {
        this.isStarted = false;
    }

    public void start() {
        if (isStarted) {
            return;
        }

        double prev = getPrevValue();
        if (prev == 0.0) {
            prev = 0.95 * getValue();
        }

        animator = ValueAnimator.ofFloat((float) prev, (float) getValue());
        animator.setDuration(DURATION);
        animator.addUpdateListener(this);
        animator.start();
        isStarted = true;
    }

    public void cancel() {
        animator.cancel();
    }

    double getPrevValue() {
        throw new RuntimeException("Stub");
    }

    double getValue() {
        throw new RuntimeException("Stub");
    }

    void setValue(double value) {
        throw new RuntimeException("Stub");
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animator) {
        double value = Double.valueOf(animator.getAnimatedValue().toString());
        setValue(value);
    }
}
