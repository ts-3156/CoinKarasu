package com.example.coinkarasu.utils;

import android.text.format.DateUtils;

public class DateHelper {

    public static String getRelativeTimeSpanString(long time, long now) {
        long diff = now - time;
        String str;

        if (diff < 1000) {
            str = "Just now";
        } else {
            str = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL).toString();
        }

        return str;
    }
}
