package com.example.coinkarasu.animator;

import android.animation.ValueAnimator;

public abstract class ValueAnimatorBase implements ValueAnimator.AnimatorUpdateListener {
    public static final long DURATION = 1000;
    private static final double THRESHOLD = 0.95;

    private boolean isStarted;
    private ValueAnimator animator;

    ValueAnimatorBase() {
        this.isStarted = false;
    }

    public void start() {
        if (isStarted) {
            return;
        }

        double cur = getValue();
        double prev = getPrevValue();

        if (prev == cur) {
            setValue(cur);
            return;
        }

        if (Math.abs(prev - cur) > (1.0 - THRESHOLD) * cur) {
            if (prev < cur) {
                prev = THRESHOLD * cur;
            } else {
                prev = (1.0 + (1.0 - THRESHOLD)) * cur;
            }
        }

        animator = ValueAnimator.ofFloat((float) prev, (float) cur);
        animator.setDuration(DURATION);
        animator.addUpdateListener(this);
        animator.start();
        isStarted = true;
    }

    public void cancel() {
        if (animator != null) {
            animator.cancel();
        }
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
