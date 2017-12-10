package com.example.toolbartest.cryptocompare.request;

import android.app.Activity;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.example.toolbartest.utils.VolleyHelper;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class BlockingRequest extends RequestBase {

    public BlockingRequest(Activity activity, String url) {
        super(activity, url);
    }

    @Override
    public JSONObject perform() {
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(getUrl(), null, future, future);

        request.setShouldCache(true);
        request.setRetryPolicy(getRetryPolicy());
        VolleyHelper.getInstance(getActivity()).addToRequestQueue(request);

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
}
