package com.example.coinkarasu.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.example.coinkarasu.activities.etc.CoinKind;
import com.example.coinkarasu.activities.etc.Exchange;
import com.example.coinkarasu.api.coincheck.data.Rate;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetCoincheckSalesRatesTask extends AsyncTask<Integer, Integer, Integer> {
    private ArrayList<GetCoincheckSalesRateThread> threads;
    private String[] fromSymbols;
    private Listener listener;
    private Context context;

    public GetCoincheckSalesRatesTask(Context context) {
        this.fromSymbols = context.getResources().getStringArray(Exchange.coincheck.salesSymbolsResId);
        this.listener = null;
        this.context = context;
        this.threads = new ArrayList<>();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        publishProgress(0);

        for (String fromSymbol : fromSymbols) {
            threads.add(new GetCoincheckSalesRateThread(context, fromSymbol));

        }

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(threads.size());

        for (GetCoincheckSalesRateThread thread : threads) {
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
            ArrayList<Rate> rates = new ArrayList<>();

            for (GetCoincheckSalesRateThread thread : threads) {
                rates.add(thread.getRate());
            }

            listener.finished(rates);
        }
    }

    public GetCoincheckSalesRatesTask setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public interface Listener {
        void started(CoinKind coinKind);

        void finished(ArrayList<Rate> rates);
    }
}