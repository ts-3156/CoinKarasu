package com.coinkarasu.api.cryptocompare.request;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.volley.RequestQueueWrapper;

import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class BlockingRequest extends RequestBase {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "BlockingRequest";

    public BlockingRequest(Context context, String url) {
        super(context, url);
    }

    public BlockingRequest(RequestQueueWrapper queue, String url) {
        super(queue, url);
    }

    public BlockingRequest(RequestQueueWrapper queue, String url, Map<String, String> headers) {
        super(queue, url, headers);
    }

    @Override
    public JSONObject perform() {
        return perform(Request.Method.GET);
    }

    @Override
    public JSONObject perform(int method, JSONObject requestBody) {
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new BlockingJsonRequest(method, url, requestBody, future);

        RetryPolicy retryPolicy;
        if (url.startsWith("http://coinkarasu.com") || url.startsWith("http://10.0.2.2") || url.startsWith("http://192.")) {
            retryPolicy = getCoinkarasuRetryPolicy();
        } else {
            retryPolicy = getDefaultRetryPolicy();
        }

        request.setRetryPolicy(retryPolicy);
        request.setShouldCache(true);
        JSONObject response = null;

        if (requestQueue.add(request) != null) {
            CKLog.time(TAG + url);
            try {
                response = future.get();
                if (DEBUG) CKLog.d(TAG, "fetch " + url + " " + CKLog.timeEnd(TAG + url));
                requestQueue.addResult(request, true);

            } catch (InterruptedException e) {
                requestQueue.addResult(request, false);
                CKLog.e(TAG, "InterruptedException " + url, e);

            } catch (ExecutionException e) {
                requestQueue.addResult(request, false);
                if (e.getMessage() != null && e.getMessage().equals("com.android.volley.TimeoutError")) {
                    CKLog.e(TAG, new RuntimeException("Timeout " + url));
                } else if (e.getMessage() != null && e.getMessage().equals("com.android.volley.ServerError")) {
                    CKLog.e(TAG, new RuntimeException("ServerError " + url));
                } else if (e.getMessage() != null && e.getMessage().startsWith("com.android.volley.NoConnectionError")) {
                    CKLog.e(TAG, new RuntimeException("NoConnectionError " + url));
                } else {
                    CKLog.e(TAG, "ExecutionException " + url, e);
                }
            }
        } else {
            if (DEBUG) CKLog.w(TAG, "skip " + url);
        }

        return response;
    }

    @Override
    public void perform(Listener listener) {
        throw new UnsupportedOperationException();
    }


    protected class BlockingJsonRequest extends JsonObjectRequest {
        BlockingJsonRequest(int method, String url, JSONObject requestBody, RequestFuture<JSONObject> future) {
            super(method, url, requestBody, future, future);
        }

        @Override
        public Map<String, String> getHeaders() {
            return headers;
        }
    }
}
