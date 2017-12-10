package com.example.toolbartest.utils;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.toolbartest.R;
import com.example.toolbartest.coins.Coin;

public class AnimHelper {

    public static final int DURATION = 1000;

    public static void setPriceAnim(final TextView view, final Coin coin) {
        double prev = coin.getPrevPrice();
        if (prev == 0.0) {
            prev = 0.95 * coin.getPrice();
        }

        ValueAnimator animator = ValueAnimator.ofFloat((float) prev, (float) coin.getPrice());
        animator.setDuration(DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                CoinPriceFormat formatter = new CoinPriceFormat(coin.getToSymbol());
                view.setText(formatter.format(animation.getAnimatedValue().toString()));
            }
        });
        animator.start();
    }

    public static void setTrendAnim(Activity activity, final TextView view, final Coin coin) {
        view.setTextColor(getTrendColor(activity, coin.getTrend()));

        double prev = coin.getPrevTrend();
        if (prev == 0.0) {
            prev = 0.95 * coin.getTrend();
        }

        ValueAnimator animator = ValueAnimator.ofFloat((float) prev, (float) coin.getTrend());
        animator.setDuration(DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                String text = StringHelper.formatTrend(Double.valueOf(animation.getAnimatedValue().toString()));
                view.setText(text);
            }
        });
        animator.start();
    }

    public static void setTrendIcon(ImageView view, Coin coin) {
        view.setImageResource(getTrendIconResId(coin.getTrend()));
    }

    public static int getTrendColor(Activity activity, double trend) {
        int color = activity.getResources().getColor(R.color.neutral_trend);

        if (trend > 0) {
            color = activity.getResources().getColor(R.color.green);
        } else if (trend < 0) {
            color = Color.RED;
        }

        return color;
    }

    private static int getTrendIconResId(double trend) {
        int resId;

        if (trend > 0) {
            resId = R.drawable.ic_trending_up;
        } else if (trend < 0) {
            resId = R.drawable.ic_trending_down;
        } else {
            resId = R.drawable.ic_trending_flat;
        }

        return resId;
    }

}
