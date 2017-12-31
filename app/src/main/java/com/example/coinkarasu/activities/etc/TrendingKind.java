package com.example.coinkarasu.activities.etc;

import com.example.coinkarasu.R;

public enum TrendingKind {
    one_hour(R.id.trending_1_hour, R.string.caption_desc_1_hour, "frag_1_hour"),
    six_hours(R.id.trending_6_hours, R.string.caption_desc_6_hours, "frag_6_hours"),
    twelve_hours(R.id.trending_12_hours, R.string.caption_desc_12_hours, "frag_12_hours"),
    twenty_four_hours(R.id.trending_24_hours, R.string.caption_desc_24_hours, "frag_24_hours"),
    three_days(R.id.trending_3_days, R.string.caption_desc_3_days, "frag_3_days");

    public int containerId;
    public int labelResId;
    public String tag;

    TrendingKind(int containerId, int labelResId, String tag) {
        this.containerId = containerId;
        this.labelResId = labelResId;
        this.tag = tag;
    }
}
