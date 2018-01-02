package com.coinkarasu.api.cryptocompare.request;

import android.content.Context;
import android.util.Log;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.coinkarasu.utils.VolleyHelper;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class BlockingRequest extends RequestBase {

    public BlockingRequest(Context context, String url) {
        super(context, url);
    }

    @Override
    public JSONObject perform() {
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(getUrl(), null, future, future);

        request.setShouldCache(true);
        request.setRetryPolicy(getRetryPolicy());
        VolleyHelper.getInstance(getContext()).addToRequestQueue(request);

        JSONObject response = null;
        Log.d("URL", getUrl());

        try {
            response = future.get();
        } catch (InterruptedException e) {
            Log.e("perform", e.getMessage() + ", " + getUrl());
        } catch (ExecutionException e) {
            Log.e("perform", e.getMessage() + ", " + getUrl());
        }

        return response;
    }
}
