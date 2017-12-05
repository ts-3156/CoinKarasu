package com.example.toolbartest.cryptocompare.request;

import android.app.Activity;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.example.toolbartest.utils.VolleyHelper;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class BlockingRequest implements Request {
    private Activity activity;
    private String url;

    public BlockingRequest(Activity activity, String url) {
        this.activity = activity;
        this.url = url;
    }

    @Override
    public JSONObject perform() {
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(url, new JSONObject(), future, future);

        request.setShouldCache(true);
        request.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyHelper.getInstance(activity).addToRequestQueue(request);

        JSONObject response = null;

        try {
            response = future.get();
        } catch (InterruptedException e) {
            Log.d("perform", e.getMessage());
        } catch (ExecutionException e) {
            Log.d("perform", e.getMessage());
        }

        return response;
    }

    @Override
    public void perform(Listener listener) {
    }
}
