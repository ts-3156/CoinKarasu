package com.example.toolbartest.utils;

import com.example.toolbartest.R;
import com.example.toolbartest.coins.Coin;

public class IconHelper {

    public static int getTrendIconResId(Coin coin) {
        return getTrendIconResId(coin.getTrend());
    }

    public static int getTrendIconResId(double trend) {
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

    public static int getWhiteTrendIconResId(double trend) {
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
