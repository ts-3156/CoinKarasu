package com.example.toolbartest.cryptocompare;

import android.app.Activity;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.toolbartest.utils.VolleyHelper;

import org.json.JSONObject;

public class Request {
    private Activity activity;
    private String url;

    public Request(Activity activity, String url) {
        this.activity = activity;
        this.url = url;
    }

    public void perform(final Listener listener) {
        JsonObjectRequest request = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.finished(response);
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyError e = new VolleyError(new String(error.networkResponse.data));
                        Log.d("perform", e.getMessage());
                    }

                });

        request.setShouldCache(true);
        request.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyHelper.getInstance(activity).addToRequestQueue(request);
    }

    public interface Listener {
        void finished(JSONObject response);
    }
}
