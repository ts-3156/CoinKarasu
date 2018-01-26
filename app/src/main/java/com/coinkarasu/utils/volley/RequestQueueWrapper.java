package com.coinkarasu.utils.volley;

import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.coinkarasu.utils.CKLog;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RequestQueueWrapper {
    private static final boolean DEBUG = false;
    private static final String TAG = "RequestQueueWrapper";

    private RequestQueue requestQueue;
    private ConcurrentMap<String, HttpHealthCache> cache;

    public RequestQueueWrapper(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
        cache = new ConcurrentHashMap<>();
    }

    public <T> void addResult(Request<T> req, boolean isSuccess) {
        if (DEBUG) CKLog.d(TAG, "addResult() " + req.getUrl() + " " + isSuccess);
        String domain = getDomainName(req.getUrl());
        if (TextUtils.isEmpty(domain)) {
            return;
        }

        cache.putIfAbsent(domain, new HttpHealthCache(domain));
        cache.get(domain).put(isSuccess);
    }

    public <T> Request<T> add(Request<T> req) {
        String domain = getDomainName(req.getUrl());
        if (TextUtils.isEmpty(domain)) {
            return null;
        }

        cache.putIfAbsent(domain, new HttpHealthCache(domain));
        HttpHealthCache status = cache.get(domain);

        if (status.isOperatingNormally()) {
            requestQueue.add(req);
            return req;
        } else {
            return null;
        }
    }

    private static String getDomainName(String url) {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            CKLog.e(TAG, url, e);
        }
        if (uri == null) {
            return null;
        }

        return uri.getHost();
    }
}
