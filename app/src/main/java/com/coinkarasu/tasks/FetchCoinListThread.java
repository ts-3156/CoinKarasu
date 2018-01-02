package com.coinkarasu.tasks;

import android.app.Activity;

import com.coinkarasu.api.cryptocompare.request.NonBlockingRequest;
import com.coinkarasu.api.cryptocompare.request.Request;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;

public class FetchCoinListThread extends Thread {
    private Activity activity;
    private CountDownLatch latch;
    private Listener listener;

    private JSONObject response;

    public FetchCoinListThread(Activity activity) {
        this.activity = activity;
        this.latch = null;
        this.response = null;
    }

    @Override
    public void run() {
        String url = "https://www.cryptocompare.com/api/data/coinlist/";
        new NonBlockingRequest(activity, url).perform(new Request.Listener() {
            @Override
            public void finished(JSONObject response) {
                FetchCoinListThread.this.response = response;
                if (listener != null) {
                    listener.finished(response);
                }
                if (latch != null) {
                    latch.countDown();
                }
            }
        });
    }

    public FetchCoinListThread setLatch(CountDownLatch latch) {
        this.latch = latch;
        return this;
    }

    public FetchCoinListThread setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public JSONObject getResponse() {
        return response;
    }

    public interface Listener {
        void finished(JSONObject response);
    }
}