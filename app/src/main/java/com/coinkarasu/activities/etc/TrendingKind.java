package com.coinkarasu.activities.etc;

import com.coinkarasu.R;

import java.util.concurrent.TimeUnit;

public enum TrendingKind {
    one_hour(R.id.trending_1_hour, R.string.home_tab_trending, R.string.caption_desc_1_hour, TimeUnit.MINUTES.toMillis(10)),
    six_hours(R.id.trending_6_hours, R.string.home_tab_trending, R.string.caption_desc_6_hours, TimeUnit.MINUTES.toMillis(30)),
    twelve_hours(R.id.trending_12_hours, R.string.home_tab_trending, R.string.caption_desc_12_hours, TimeUnit.MINUTES.toMillis(60)),
    twenty_four_hours(R.id.trending_24_hours, R.string.home_tab_trending, R.string.caption_desc_24_hours, TimeUnit.MINUTES.toMillis(60)),
    three_days(R.id.trending_3_days, R.string.home_tab_trending, R.string.caption_desc_3_days, TimeUnit.MINUTES.toMillis(60)),
    all_in_one_hour(R.id.all_in_one_hour, R.string.home_tab_all, R.string.caption_desc_1_hour, TimeUnit.MINUTES.toMillis(10));

    public int containerId;
    public int titleResId;
    public int captionResId;
    public long expiration;

    TrendingKind(int containerId, int titleResId, int captionResId, long expiration) {
        this.containerId = containerId;
        this.titleResId = titleResId;
        this.captionResId = captionResId;
        this.expiration = expiration;
    }

    public String tag() {
        return "frag_" + name();
    }
}
