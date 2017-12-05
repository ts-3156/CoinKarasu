package com.example.toolbartest.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.example.toolbartest.cryptocompare.Request;
import com.example.toolbartest.utils.StringHelper;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;

public class FetchCoinListTask extends AsyncTask<Integer, Integer, Integer> {
    private JSONObject coinListResponse;

    private Activity activity;
    private Listener listener;

    private FetchPricesThread internalThread;

    public FetchCoinListTask(Activity activity) {
        this.coinListResponse = null;

        this.activity = activity;
        this.listener = null;

        internalThread = new FetchPricesThread(activity);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        CountDownLatch latch = new CountDownLatch(2);
        internalThread.setLatch(latch).start();
        fetchCoinList(latch);

        try {
            latch.await();
        } catch (InterruptedException e) {
        }

        return 200;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (listener != null) {
            listener.finished(coinListResponse, internalThread.getCoinPricesResponse());
        }
    }

    private void fetchCoinList(final CountDownLatch latch) {
        String url = "https://www.cryptocompare.com/api/data/coinlist/";
        new Request(activity, url).perform(new Request.Listener() {
            @Override
            public void finished(JSONObject response) {
                FetchCoinListTask.this.coinListResponse = response;
                latch.countDown();
            }
        });
    }

    public FetchCoinListTask setFromSymbols(String[] fromSymbols) {
        internalThread.setFromSymbols(fromSymbols);
        return this;
    }

    public FetchCoinListTask setToSymbol(String toSymbol) {
        internalThread.setToSymbol(toSymbol);
        return this;
    }

    public FetchCoinListTask setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public interface Listener {
        void finished(JSONObject coinListResponse, JSONObject coinPricesResponse);
    }
}