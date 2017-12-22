package com.example.coinkarasu.tasks;

import android.os.AsyncTask;

import com.example.coinkarasu.api.cryptocompare.Client;
import com.example.coinkarasu.api.cryptocompare.data.Prices;
import com.example.coinkarasu.api.cryptocompare.data.PricesImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetPricesTask extends AsyncTask<Integer, Integer, Integer> {
    private Listener listener;
    private Client client;
    private ArrayList<GetPricesThread> threads;
    private String[] fromSymbols;
    private String toSymbol;
    private String exchange;

    public GetPricesTask(Client client) {
        this.listener = null;
        this.client = client;
        this.threads = new ArrayList<>();
        this.fromSymbols = null;
        this.toSymbol = null;
        this.exchange = null;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        publishProgress(0);

        for (int i = 0; i < fromSymbols.length; i += 20) {
            int index = i + 19;
            if (index >= fromSymbols.length) {
                index = fromSymbols.length - 1;
            }

            String[] target = Arrays.copyOfRange(fromSymbols, i, index + 1);
            threads.add(new GetPricesThread(client, target, toSymbol, exchange));

            if (index >= fromSymbols.length) {
                break;
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(threads.size());

        for (GetPricesThread thread : threads) {
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
            listener.started(exchange, fromSymbols, toSymbol);
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (listener != null) {
            Prices prices = new PricesImpl(threads.get(0).getExchange());

            for (GetPricesThread thread : threads) {
                prices.merge(thread.getPrices());
            }

            listener.finished(prices);
        }
    }

    public GetPricesTask setFromSymbols(String[] fromSymbols) {
        this.fromSymbols = fromSymbols;
        return this;
    }

    public GetPricesTask setToSymbol(String toSymbol) {
        this.toSymbol = toSymbol;
        return this;
    }

    public GetPricesTask setExchange(String exchange) {
        this.exchange = exchange;
        return this;
    }

    public GetPricesTask setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public interface Listener {
        void started(String exchange, String[] fromSymbols, String toSymbol);

        void finished(Prices prices);
    }
}