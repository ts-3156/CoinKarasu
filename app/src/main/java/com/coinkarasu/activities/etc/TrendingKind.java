package com.coinkarasu.activities.etc;

import com.coinkarasu.R;

import java.util.concurrent.TimeUnit;

public enum TrendingKind {
    one_hour(R.id.trending_1_hour, R.string.caption_desc_1_hour, "frag_1_hour", TimeUnit.MINUTES.toMillis(10)),
    six_hours(R.id.trending_6_hours, R.string.caption_desc_6_hours, "frag_6_hours", TimeUnit.MINUTES.toMillis(30)),
    twelve_hours(R.id.trending_12_hours, R.string.caption_desc_12_hours, "frag_12_hours", TimeUnit.MINUTES.toMillis(60)),
    twenty_four_hours(R.id.trending_24_hours, R.string.caption_desc_24_hours, "frag_24_hours", TimeUnit.MINUTES.toMillis(60)),
    three_days(R.id.trending_3_days, R.string.caption_desc_3_days, "frag_3_days", TimeUnit.MINUTES.toMillis(60));

    public int containerId;
    public int labelResId;
    public String tag;
    public long expiration;

    TrendingKind(int containerId, int labelResId, String tag, long expiration) {
        this.containerId = containerId;
        this.labelResId = labelResId;
        this.tag = tag;
        this.expiration = expiration;
    }
}
