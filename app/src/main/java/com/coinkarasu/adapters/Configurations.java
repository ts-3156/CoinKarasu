package com.coinkarasu.adapters;

import android.content.Context;

import com.coinkarasu.activities.etc.Section;
import com.coinkarasu.utils.PrefHelper;

import java.util.HashSet;
import java.util.Set;

public class Configurations {
    public boolean isDownloadIconEnabled;
    public boolean isAnimEnabled;
    public boolean isBeingScrolled;
    private Set<Section> isAnimStarted;
    public boolean isAirplaneModeOn;

    public Configurations(Context context) {
        isBeingScrolled = false;
        isAnimStarted = new HashSet<>();
        loadData(context);
    }

    public void startAnimation(Section section) {
        isAnimStarted.add(section);
    }

    public boolean isAnimStarted(Section section) {
        return isAnimStarted.contains(section);
    }

    public void loadData(Context context) {
        isAnimEnabled = PrefHelper.shouldAnimatePrice(context);
        isDownloadIconEnabled = PrefHelper.shouldDownloadIcon(context);
        isAirplaneModeOn = PrefHelper.isAirplaneModeOn(context);
    }
}
