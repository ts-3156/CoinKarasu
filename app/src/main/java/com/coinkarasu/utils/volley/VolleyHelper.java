package com.coinkarasu.utils.volley;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

// https://developer.android.com/training/volley/requestqueue.html
public class VolleyHelper {
    private static VolleyHelper instance;

    private RequestQueue requestQueue;
    private ImageLoader imageLoader;

    private VolleyHelper(Context appContext) {
        requestQueue = Volley.newRequestQueue(appContext);
        imageLoader = new ImageLoader(requestQueue, new DiskCache(appContext.getCacheDir()));
    }

    public static synchronized VolleyHelper getInstance(Context context) {
        if (instance == null) {
            instance = new VolleyHelper(context.getApplicationContext());
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

}