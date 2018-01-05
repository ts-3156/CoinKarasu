package com.coinkarasu.utils.volley;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

class MemoryCache implements ImageLoader.ImageCache {
    private LruCache<String, Bitmap> cache;

    MemoryCache() {
        cache = new LruCache<>(20);
    }

    @Override
    public Bitmap getBitmap(String url) {
        return cache.get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        if (getBitmap(url) == null) {
            cache.put(url, bitmap);
        }
    }
}
