package com.coinkarasu.utils.volley;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.android.volley.toolbox.ImageLoader;

import java.io.File;

public class DiskCache implements ImageLoader.ImageCache {
    private MemoryCache memCache;
    private File rootDir;

    DiskCache(File rootDir) {
        this.rootDir = rootDir;
        this.memCache = new MemoryCache();
    }

    @Override
    public Bitmap getBitmap(String url) {
        Bitmap bitmap = memCache.getBitmap(url);
        if (bitmap == null) {
            File file = getFileForUrl(url);
            if (file.exists()) {
                bitmap = BitmapUtils.read(file);
            }
        }
        return bitmap;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        if (memCache.getBitmap(url) == null) {
            memCache.putBitmap(url, bitmap);
        }
        File file = getFileForUrl(url);
        if (!file.exists()) {
            new WriteBitmapToDiskTask(file).execute(bitmap);
        }
    }

    private static String urlToFileName(String url) {
        String fileName = url.substring(url.lastIndexOf('/') + 1, url.length());
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    private File getFileForUrl(String url) {
        return new File(rootDir, urlToFileName(url));
    }

    private static class WriteBitmapToDiskTask extends AsyncTask<Bitmap, Void, Void> {
        private File file;

        WriteBitmapToDiskTask(File file) {
            this.file = file;
        }

        @Override
        protected Void doInBackground(Bitmap... params) {
            BitmapUtils.write(file, params[0]);
            return null;
        }
    }
}
