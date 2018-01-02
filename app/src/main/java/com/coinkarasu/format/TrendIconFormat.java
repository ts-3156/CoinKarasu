package com.coinkarasu.format;

import com.coinkarasu.R;

public class TrendIconFormat {
    public TrendIconFormat() {
    }

    public int format(double trend) {
        return format(trend, false);
    }

    public int format(double trend, boolean isSelected) {
        int resId;

        if (isSelected) {
            if (trend > 0.0) {
                resId = R.drawable.ic_trending_up_white;
            } else if (trend < 0.0) {
                resId = R.drawable.ic_trending_down_white;
            } else {
                resId = R.drawable.ic_trending_flat_white;
            }
        }else {
            if (trend > 0.0) {
                resId = R.drawable.ic_trending_up;
            } else if (trend < 0.0) {
                resId = R.drawable.ic_trending_down;
            } else {
                resId = R.drawable.ic_trending_flat;
            }
        }

        return resId;
    }
}
