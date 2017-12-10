package com.example.toolbartest.format;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.widget.TextView;

import com.example.toolbartest.R;
import com.example.toolbartest.coins.AggregatedData;
import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.utils.StringHelper;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class TrendViewFormat {
    private static final long DURATION = 1000;

    private static final int[] colors = {
            Color.RED, Color.parseColor("#8a000000"), Color.parseColor("#008000")};

    private double prevTrend;
    private double curTrend;
    private boolean anim;

    public TrendViewFormat(Coin coin) {
        this(-1.0, coin.getTrend(), false);
    }

    public TrendViewFormat(Coin coin, boolean anim) {
        this(coin.getPrevTrend(), coin.getTrend(), anim);
    }

    private TrendViewFormat(String curTrend) {
        this(-1.0, Double.valueOf(curTrend), false);
    }

    private TrendViewFormat(double prevTrend, double curTrend, boolean anim) {
        this.prevTrend = prevTrend;
        this.curTrend = curTrend;
        this.anim = anim;
    }

    public void format(TextView view) {
        view.setTextColor(getTrendColor(curTrend));

        NumberFormat formatter = NumberFormat.getPercentInstance();
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);
        view.setText(formatter.format(curTrend));

        if (anim && prevTrend != -0.1) {
            setAnim(view);
        }
    }

    private void setAnim(final TextView view) {
        double prev = prevTrend;
        if (prev == 0.0) {
            prev = 0.95 * curTrend;
        }

        ValueAnimator animator = ValueAnimator.ofFloat((float) prev, (float) curTrend);
        animator.setDuration(DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                new TrendViewFormat(animation.getAnimatedValue().toString()).format(view);
            }
        });
        animator.start();
    }

    private int getTrendColor(double trend) {
        int color;

        if (trend < 0) {
            color = colors[0];
        } else if (trend > 0) {
            color = colors[2];
        } else {
            color = colors[1];
        }

        return color;
    }

}
