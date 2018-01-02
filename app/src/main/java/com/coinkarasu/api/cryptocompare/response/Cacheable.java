package com.coinkarasu.api.cryptocompare.response;

import android.content.Context;

public interface Cacheable {
    boolean saveToCache(Context context);

    boolean saveToCache(Context context, String tag);

    boolean isCache();
}
