package com.coinkarasu.api.cryptocompare.request;

import android.app.Activity;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.coinkarasu.utils.VolleyHelper;

import org.json.JSONObject;

public class NonBlockingRequest extends RequestBase {

    public NonBlockingRequest(Activity activity, String url) {
        super(activity, url);
    }

    @Override
    public void perform(final Listener listener) {
        // TODO Bug fix
        JsonObjectRequest request = new JsonObjectRequest(com.android.volley.Request.Method.GET, getUrl(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (listener != null) {
                            listener.finished(response);
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("onErrRes", getUrl());
                        VolleyError e = new VolleyError(new String(error.networkResponse.data));
                        throw new RuntimeException(e.getMessage());
                    }

                });

        request.setShouldCache(true);
        request.setRetryPolicy(getRetryPolicy());
        VolleyHelper.getInstance(getContext()).addToRequestQueue(request);
    }
}
