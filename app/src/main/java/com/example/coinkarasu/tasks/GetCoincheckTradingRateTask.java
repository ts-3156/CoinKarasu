package com.example.coinkarasu.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.example.coinkarasu.activities.etc.CoinKind;
import com.example.coinkarasu.api.coincheck.data.Rate;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetCoincheckTradingRateTask extends AsyncTask<Integer, Integer, Integer> {
    private ArrayList<GetCoincheckTradingRateThread> threads;
    private Listener listener;
    private Context context;

    public GetCoincheckTradingRateTask(Context context) {
        this.listener = null;
        this.context = context;
        this.threads = new ArrayList<>();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        publishProgress(0);

        threads.add(new GetCoincheckTradingRateThread(context, "sell"));
        threads.add(new GetCoincheckTradingRateThread(context, "buy"));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(threads.size());

        for (GetCoincheckTradingRateThread thread : threads) {
            thread.setLatch(latch);
            executor.submit(thread);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
        }

        executor.shutdown();

        return 200;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        if (listener != null) {
            listener.started(CoinKind.trading);
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (listener != null) {
            double sum = 0.0;

            for (GetCoincheckTradingRateThread thread : threads) {
                sum += thread.getRate().value;
            }

            Rate rate = threads.get(0).getRate();
            new Rate(rate.fromSymbol, rate.toSymbol, sum / threads.size());

            listener.finished(rate);
        }
    }

    public GetCoincheckTradingRateTask setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public interface Listener {
        void started(CoinKind coinKind);

        void finished(Rate rate);
    }
}