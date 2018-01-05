package com.coinkarasu.utils.volley;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.android.volley.toolbox.ImageLoader;

public class DiskCache implements ImageLoader.ImageCache {
    private MemoryCache memCache;
    private Context context;

    DiskCache(Context context) {
        this.context = context;
        this.memCache = new MemoryCache();
    }

    @Override
    public Bitmap getBitmap(String url) {
        Bitmap bitmap = memCache.getBitmap(url);
        if (bitmap == null && IconDiskCacheHelper.exists(context, urlToFileName(url))) {
            bitmap = IconDiskCacheHelper.read(context, urlToFileName(url));
        }
        return bitmap;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        if (memCache.getBitmap(url) == null) {
            memCache.putBitmap(url, bitmap);
        }
        if (!IconDiskCacheHelper.exists(context, urlToFileName(url))) {
            new WriteBitmapToDiskTask(context, url, bitmap).execute();
        }
    }

    private static String urlToFileName(String url) {
        String fileName = url.substring(url.lastIndexOf('/') + 1, url.length());
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    private static class WriteBitmapToDiskTask extends AsyncTask<Void, Void, Void> {
        private Context context;
        private String url;
        private Bitmap bitmap;

        WriteBitmapToDiskTask(Context context, String url, Bitmap bitmap) {
            this.context = context;
            this.url = url;
            this.bitmap = bitmap;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            IconDiskCacheHelper.write(context, urlToFileName(url), bitmap);
            return null;
        }
    }
}
