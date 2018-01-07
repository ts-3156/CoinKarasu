package com.coinkarasu.adapters;

import android.content.Context;

import com.coinkarasu.utils.PrefHelper;

public class ConfigUtils {
    public boolean isDownloadIconEnabled;
    public boolean isAnimPaused;
    public boolean isAnimEnabled;
    public boolean isScrolled;

    public ConfigUtils(Context context) {
        isAnimEnabled = PrefHelper.isAnimEnabled(context);
        isDownloadIconEnabled = PrefHelper.isDownloadIconEnabled(context);
        isScrolled = false;
        isAnimPaused = false;
    }
}
