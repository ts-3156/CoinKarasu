package com.coinkarasu.activities.etc;

import com.coinkarasu.R;

import java.util.concurrent.TimeUnit;

public enum TrendingKind {
    one_hour(R.id.trending_1_hour, R.string.home_tab_available_in_japan, R.string.home_tab_duration_1_hour, TimeUnit.MINUTES.toMillis(10), false, true),
    six_hours(R.id.trending_6_hours, R.string.home_tab_available_in_japan, R.string.home_tab_duration_6_hours, TimeUnit.MINUTES.toMillis(30), true, false),
    twelve_hours(R.id.trending_12_hours, R.string.home_tab_available_in_japan, R.string.home_tab_duration_12_hours, TimeUnit.MINUTES.toMillis(60), true, false),
    twenty_four_hours(R.id.trending_24_hours, R.string.home_tab_available_in_japan, R.string.home_tab_duration_24_hours, TimeUnit.MINUTES.toMillis(60), true, false),
    three_days(R.id.trending_3_days, R.string.home_tab_available_in_japan, R.string.home_tab_duration_3_days, TimeUnit.MINUTES.toMillis(60), true, false);

    public int containerId;
    public int titleResId;
    public int durationResId;
    public long expiration;
    public boolean filterOnlyTrending;
    public boolean shouldUseGridLayout;

    TrendingKind(int containerId, int titleResId, int durationResId, long expiration, boolean filterOnlyTrending, boolean shouldUseGridLayout) {
        this.containerId = containerId;
        this.titleResId = titleResId;
        this.durationResId = durationResId;
        this.expiration = expiration;
        this.filterOnlyTrending = filterOnlyTrending;
        this.shouldUseGridLayout = shouldUseGridLayout;
    }

    public String tag() {
        return "frag_" + name();
    }
}
