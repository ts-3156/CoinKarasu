package com.example.toolbartest.tasks;

import android.app.Activity;
import android.util.Log;

import com.example.toolbartest.cryptocompare.Request;
import com.example.toolbartest.utils.StringHelper;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;

public class FetchPricesThread extends Thread {
    private Activity activity;
    private CountDownLatch latch;

    private JSONObject coinPricesResponse;

    private String[] fromSymbols;
    private String toSymbol;

    public FetchPricesThread(Activity activity) {
        this.activity = activity;
        this.latch = null;
        this.coinPricesResponse = null;
    }

    @Override
    public void run() {
        fetchCoinPrices();
    }

    private void fetchCoinPrices() {
        String url = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=" + StringHelper.join(",", fromSymbols) + "&tsyms=" + toSymbol;
        new Request(activity, url).perform(new Request.Listener() {
            @Override
            public void finished(JSONObject response) {
                FetchPricesThread.this.coinPricesResponse = response;
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

    public JSONObject getCoinPricesResponse() {
        return coinPricesResponse;
    }
}