package com.example.coinkarasu.tasks;

import android.app.Activity;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;

public class FetchPricesTask extends AsyncTask<Integer, Integer, Integer> {
    private Listener listener;

    private FetchPricesThread internalThread;

    public FetchPricesTask(Activity activity) {
        this.listener = null;
        internalThread = new FetchPricesThread(activity);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        CountDownLatch latch = new CountDownLatch(1);
        internalThread.setLatch(latch).start();

        try {
            latch.await();
        } catch (InterruptedException e) {
        }

        return 200;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (listener != null) {
            listener.finished(internalThread.getResponse());
        }
    }

    public FetchPricesTask setFromSymbols(String[] fromSymbols) {
        internalThread.setFromSymbols(fromSymbols);
        return this;
    }

    public FetchPricesTask setToSymbol(String toSymbol) {
        internalThread.setToSymbol(toSymbol);
        return this;
    }

    public FetchPricesTask setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public interface Listener {
        void finished(JSONObject coinPricesResponse);
    }
}