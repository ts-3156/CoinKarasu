package com.example.coinkarasu.cryptocompare.request;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;

import org.json.JSONObject;

public class RequestBase implements Request {
    private Context context;
    private String url;

    RequestBase(Context context, String url) {
        this.context = context;
        this.url = url;
    }

    @Override
    public JSONObject perform() {
        throw new RuntimeException("Stub");
    }

    @Override
    public void perform(Listener listener) {
        throw new RuntimeException("Stub");
    }

    DefaultRetryPolicy getRetryPolicy() {
        return new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    }

    Context getContext() {
        return context;
    }

    String getUrl() {
        return url;
    }
}
