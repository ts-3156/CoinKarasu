package com.coinkarasu.animator;

import android.animation.ValueAnimator;

import com.coinkarasu.utils.CKLog;

public abstract class ValueAnimatorBase implements ValueAnimator.AnimatorUpdateListener {
    private static final boolean DEBUG = true;
    private static final String TAG = "ValueAnimatorBase";
    public static final long DURATION = 1000;
    private static final double THRESHOLD_PCT = 0.95;
    private static final double THRESHOLD_VAL = 100.0;

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

        if (Math.abs(prev - cur) > (1.0 - THRESHOLD_PCT) * cur) {
            if (prev < cur) {
                prev = THRESHOLD_PCT * cur;
            } else {
                prev = (1.0 + (1.0 - THRESHOLD_PCT)) * cur;
            }
        }

        if (Math.abs(prev - cur) > THRESHOLD_VAL) {
            if (prev < cur) {
                prev = cur - THRESHOLD_VAL;
            } else {
                prev = cur + THRESHOLD_VAL;
            }
        }

        if (DEBUG) CKLog.d(TAG, "start() prev=" + prev + " cur=" + cur);

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
        setValue(getValue());
    }

    abstract double getPrevValue();

    abstract double getValue();

    abstract void setValue(double value);

    @Override
    public void onAnimationUpdate(ValueAnimator animator) {
        double value = Double.valueOf(animator.getAnimatedValue().toString());
        setValue(value);
    }
}
