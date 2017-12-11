package com.example.toolbartest.utils;

import android.widget.ImageView;

import com.example.toolbartest.R;
import com.example.toolbartest.coins.Coin;

public class AnimHelper {

    public static void setTrendIcon(ImageView view, Coin coin) {
        view.setImageResource(getTrendIconResId(coin.getTrend()));
    }

    public static void setTrendIcon(ImageView view, double trend) {
        view.setImageResource(getTrendIconResId(trend));
    }

    public static void setWhiteTrendIcon(ImageView view, double trend) {
        view.setImageResource(getWhiteTrendIconResId(trend));
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

    private static int getWhiteTrendIconResId(double trend) {
        int resId;

        if (trend > 0) {
            resId = R.drawable.ic_trending_up_white;
        } else if (trend < 0) {
            resId = R.drawable.ic_trending_down_white;
        } else {
            resId = R.drawable.ic_trending_flat_white;
        }

        return resId;
    }

}
