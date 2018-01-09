package com.coinkarasu.api.cryptocompare.request;

import android.content.Context;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.volley.VolleyHelper;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class BlockingRequest extends RequestBase {
    private static final boolean DEBUG = true;
    private static final String TAG = "BlockingRequest";

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
        if (DEBUG) CKLog.d(TAG, getUrl());

        try {
            response = future.get();
        } catch (InterruptedException e) {
            if (DEBUG) CKLog.e(TAG, getUrl(), e);
        } catch (ExecutionException e) {
            if (DEBUG) CKLog.e(TAG, getUrl(), e);
        }

        return response;
    }
}
