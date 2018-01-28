package com.coinkarasu.api.cryptocompare.request;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.coinkarasu.BuildConfig;
import com.coinkarasu.utils.volley.RequestQueueWrapper;
import com.coinkarasu.utils.volley.VolleyHelper;

import org.json.JSONObject;

import java.util.Collections;
import java.util.Map;

import static com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
import static com.android.volley.DefaultRetryPolicy.DEFAULT_MAX_RETRIES;
import static com.android.volley.DefaultRetryPolicy.DEFAULT_TIMEOUT_MS;

public abstract class RequestBase implements Request {
    protected RequestQueueWrapper requestQueue;
    protected String url;
    protected Map<String, String> headers;

    RequestBase(Context context, String url) {
        this(VolleyHelper.getInstance(context).getWrappedRequestQueue(), url);
    }

    RequestBase(RequestQueueWrapper queue, String url) {
        this(queue, url, Collections.<String, String>emptyMap());
    }

    RequestBase(RequestQueueWrapper queue, String url, Map<String, String> headers) {
        this.requestQueue = queue;
        this.url = url;
        this.headers = headers;
    }

    @Override
    public abstract JSONObject perform();

    public JSONObject perform(int method) {
        return perform(method, null);
    }

    public abstract JSONObject perform(int method, JSONObject requestBody);

    @Override
    public abstract void perform(Listener listener);

    private static final int CK_TIMEOUT_MS = BuildConfig.DEBUG ? 500 : 1000;
    private static final int CK_MAX_RETRIES = BuildConfig.DEBUG ? 0 : 1;
    private static final float CK_BACKOFF_MULT = 1f;

    DefaultRetryPolicy getDefaultRetryPolicy() {
        return new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES, DEFAULT_BACKOFF_MULT);
    }

    DefaultRetryPolicy getCoinkarasuRetryPolicy() {
        return new DefaultRetryPolicy(CK_TIMEOUT_MS, CK_MAX_RETRIES, CK_BACKOFF_MULT);
    }
}
