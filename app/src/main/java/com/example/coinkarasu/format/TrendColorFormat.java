package com.example.coinkarasu.format;

import com.example.coinkarasu.R;

public class TrendColorFormat {
    public TrendColorFormat() {
    }

    public int format(double trend) {
        int color;

        if (trend > 0.0) {
            color = R.color.colorTrendUp;
        } else if (trend < 0.0) {
            color = R.color.colorTrendDown;
        } else {
            color = R.color.colorTrendFlat;
        }

        return color;
    }
}
