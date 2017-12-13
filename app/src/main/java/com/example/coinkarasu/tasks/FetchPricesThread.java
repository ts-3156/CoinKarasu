package com.example.coinkarasu.tasks;

import android.app.Activity;

import com.example.coinkarasu.cryptocompare.request.NonBlockingRequest;
import com.example.coinkarasu.cryptocompare.request.Request;
import com.example.coinkarasu.utils.StringHelper;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;

public class FetchPricesThread extends Thread {
    private Activity activity;
    private CountDownLatch latch;

    private JSONObject response;

    private String[] fromSymbols;
    private String toSymbol;

    public FetchPricesThread(Activity activity) {
        this.activity = activity;
        this.latch = null;
        this.response = null;
    }

    @Override
    public void run() {
        String url = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=" + StringHelper.join(",", fromSymbols) + "&tsyms=" + toSymbol;
        new NonBlockingRequest(activity, url).perform(new Request.Listener() {
            @Override
            public void finished(JSONObject response) {
                FetchPricesThread.this.response = response;
                if (latch != null) {
                    latch.countDown();
                }
            }
        });
    }

    public FetchPricesThread setFromSymbols(String[] fromSymbols) {
        this.fromSymbols = fromSymbols;
        return this;
    }

    public FetchPricesThread setToSymbol(String toSymbol) {
        this.toSymbol = toSymbol;
        return this;
    }

    public FetchPricesThread setLatch(CountDownLatch latch) {
        this.latch = latch;
        return this;
    }

    public JSONObject getResponse() {
        return response;
    }
}