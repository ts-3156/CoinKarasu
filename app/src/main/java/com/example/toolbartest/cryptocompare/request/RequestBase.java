package com.example.toolbartest.cryptocompare.request;

import android.app.Activity;

import com.android.volley.DefaultRetryPolicy;

import org.json.JSONObject;

public class RequestBase implements Request {
    private Activity activity;
    private String url;

    RequestBase(Activity activity, String url) {
        this.activity = activity;
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

    Activity getActivity() {
        return activity;
    }

    String getUrl() {
        return url;
    }
}
