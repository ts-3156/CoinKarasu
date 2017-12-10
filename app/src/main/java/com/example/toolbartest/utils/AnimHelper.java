package com.example.toolbartest.utils;

import android.widget.ImageView;

import com.example.toolbartest.R;
import com.example.toolbartest.coins.Coin;

public class AnimHelper {

    public static void setTrendIcon(ImageView view, Coin coin) {
        view.setImageResource(getTrendIconResId(coin.getTrend()));
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
