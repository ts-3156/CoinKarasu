package com.example.toolbartest.tasks;

import android.app.Activity;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;

public class FetchCoinListTask extends AsyncTask<Integer, Integer, Integer> {
    private Listener listener;
    private FetchCoinListThread fetchCoinListThread;
    private FetchPricesThread fetchPricesThread;

    public FetchCoinListTask(Activity activity) {
        listener = null;
        fetchCoinListThread = new FetchCoinListThread(activity);
        fetchPricesThread = new FetchPricesThread(activity);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        CountDownLatch latch = new CountDownLatch(2);
        fetchCoinListThread.setLatch(latch).start();
        fetchPricesThread.setLatch(latch).start();

        try {
            latch.await();
        } catch (InterruptedException e) {
        }

        return 200;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (listener != null) {
            listener.finished(fetchCoinListThread.getResponse(), fetchPricesThread.getResponse());
        }
    }

    public FetchCoinListTask setFromSymbols(String[] fromSymbols) {
        fetchPricesThread.setFromSymbols(fromSymbols);
        return this;
    }

    public FetchCoinListTask setToSymbol(String toSymbol) {
        fetchPricesThread.setToSymbol(toSymbol);
        return this;
    }

    public FetchCoinListTask setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public interface Listener {
        void finished(JSONObject coinListResponse, JSONObject pricesResponse);
    }
}