package com.coinkarasu.adapters;

import android.content.Context;

import com.coinkarasu.activities.etc.Section;
import com.coinkarasu.utils.PrefHelper;

import java.util.HashSet;
import java.util.Set;

public class ConfigUtils {
    public boolean isDownloadIconEnabled;
    public boolean isAnimEnabled;
    public boolean isScrolled;
    private Set<Section> isAnimStarted;

    public ConfigUtils(Context context) {
        isAnimEnabled = PrefHelper.shouldAnimatePrices(context);
        isDownloadIconEnabled = PrefHelper.shouldDownloadIcon(context);
        isScrolled = false;
        isAnimStarted = new HashSet<>();
    }

    public void startAnimation(Section section) {
        isAnimStarted.add(section);
    }

    public boolean isAnimStarted(Section section) {
        return isAnimStarted.contains(section);
    }
}
