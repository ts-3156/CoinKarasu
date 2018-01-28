package com.coinkarasu.api.cryptocompare.request;

import android.app.Activity;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

public class NonBlockingRequest extends RequestBase {

    public NonBlockingRequest(Activity activity, String url) {
        super(activity, url);
    }

    @Override
    public void perform(final Listener listener) {
        // TODO Bug fix
        JsonObjectRequest request = new JsonObjectRequest(url, null,
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
                        Log.d("onErrRes", url);
                        VolleyError e = new VolleyError(new String(error.networkResponse.data));
                        throw new RuntimeException(e.getMessage());
                    }

                });

        request.setShouldCache(true);
        request.setRetryPolicy(getDefaultRetryPolicy());
        requestQueue.add(request);
    }

    @Override
    public JSONObject perform() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject perform(int method) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject perform(int method, JSONObject requestBody) {
        throw new UnsupportedOperationException();
    }
}
