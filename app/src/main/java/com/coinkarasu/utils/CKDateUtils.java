package com.coinkarasu.utils;

import android.text.format.DateUtils;

public class CKDateUtils {
    public static CharSequence getRelativeTimeSpanString(long time, long now) {
        return DateUtils.getRelativeTimeSpanString(time, now,
                DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
    }

    public static CharSequence getRelativeTimeSpanString(long time) {
        return getRelativeTimeSpanString(time, CKDateUtils.now());
    }

    public static long now() {
        return System.currentTimeMillis();
    }
}
