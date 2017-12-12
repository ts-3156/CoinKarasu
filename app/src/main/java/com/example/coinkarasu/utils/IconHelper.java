package com.example.coinkarasu.utils;

import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.R;

public class IconHelper {

    public static int getTrendIconResId(Coin coin) {
        return getTrendIconResId(coin.getTrend());
    }

    public static int getTrendIconResId(String trend) {
        String str = trend.replace("%", "").replace(",", "");

        int resId;

        switch (str) {
            case "∞":
                resId = R.drawable.ic_trending_up;
                break;
            case "-∞":
                resId = R.drawable.ic_trending_down;
                break;
            default:
                resId = getTrendIconResId(Double.valueOf(str));
                break;
        }

        return resId;
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

    public static int getWhiteTrendIconResId(String trend) {
        String str = trend.replace("%", "").replace(",", "");

        int resId;

        switch (str) {
            case "∞":
                resId = R.drawable.ic_trending_up_white;
                break;
            case "-∞":
                resId = R.drawable.ic_trending_down_white;
                break;
            default:
                resId = getWhiteTrendIconResId(Double.valueOf(str));
                break;
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
