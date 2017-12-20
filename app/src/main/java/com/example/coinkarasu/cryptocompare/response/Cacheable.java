package com.example.coinkarasu.cryptocompare.response;

import android.content.Context;

public interface Cacheable {
    boolean saveToCache(Context context);

    boolean saveToCache(Context context, String tag);

    boolean isCache();
}
