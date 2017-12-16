package com.example.coinkarasu.format;

import android.graphics.Color;

public class TrendColorFormat {
    private static final int[] colors = {
            Color.RED, Color.parseColor("#8a000000"), Color.parseColor("#008000")};

    public TrendColorFormat() {
    }

    public int format(double trend) {
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
