package com.example.coinkarasu.tasks;

import android.os.AsyncTask;

import com.example.coinkarasu.api.cryptocompare.Client;
import com.example.coinkarasu.api.cryptocompare.data.History;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetMultipleHistoryDayTask extends AsyncTask<Integer, Integer, Integer> {
    private Listener listener;
    private Client client;
    private ArrayList<GetHistoryDayThread> threads;
    private String fromSymbol;
    private String toSymbol;
    private String[] exchanges;

    public GetMultipleHistoryDayTask(Client client) {
        this.listener = null;
        this.client = client;
        this.threads = new ArrayList<>();
        this.fromSymbol = null;
        this.toSymbol = null;
        this.exchanges = null;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        publishProgress(0);

        for (String exchange : exchanges) {
            threads.add(new GetHistoryDayThread(client, fromSymbol, toSymbol, exchange));
        }

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(threads.size());

        for (GetHistoryDayThread thread : threads) {
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
    protected void onPostExecute(Integer integer) {
        if (listener != null) {
            HashMap<String, ArrayList<History>> map = new HashMap<>();

            for (GetHistoryDayThread thread : threads) {
                map.put(thread.getExchange(), thread.getHistories());
            }

            listener.finished(map);
        }
    }

    public GetMultipleHistoryDayTask setFromSymbol(String fromSymbol) {
        this.fromSymbol = fromSymbol;
        return this;
    }

    public GetMultipleHistoryDayTask setToSymbol(String toSymbol) {
        this.toSymbol = toSymbol;
        return this;
    }

    public GetMultipleHistoryDayTask setExchanges(String[] exchanges) {
        this.exchanges = exchanges;
        return this;
    }

    public GetMultipleHistoryDayTask setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public interface Listener {
        void finished(HashMap<String, ArrayList<History>> map);
    }
}