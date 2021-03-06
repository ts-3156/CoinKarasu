package com.coinkarasu.animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;

public abstract class ColorAnimatorBase implements ValueAnimator.AnimatorUpdateListener {
    private static final long DURATION = ValueAnimatorBase.DURATION;

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

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ColorAnimatorBase.this.onAnimationEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                ColorAnimatorBase.this.onAnimationEnd();
            }
        });

        animator.start();
        isStarted = true;
    }

    public void cancel() {
        animator.cancel();
        setValue(getValue());
    }

    abstract int getPrevValue();

    abstract int getValue();

    abstract void setValue(int value);

    abstract void onAnimationEnd();

    @Override
    public void onAnimationUpdate(ValueAnimator animator) {
        setValue((int) animator.getAnimatedValue());
    }
}
