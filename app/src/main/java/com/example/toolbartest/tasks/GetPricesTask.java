package com.example.toolbartest.tasks;

import android.os.AsyncTask;

import com.example.toolbartest.cryptocompare.Client;
import com.example.toolbartest.cryptocompare.data.Prices;

public class GetPricesTask extends AsyncTask<Integer, Integer, Integer> {
    private Listener listener;
    private Client client;
    private Prices prices;
    private String[] fromSymbols;
    private String toSymbol;
    private String exchange;

    public GetPricesTask(Client client) {
        this.listener = null;
        this.client = client;
        this.prices = null;
        this.fromSymbols = null;
        this.toSymbol = null;
        this.exchange = null;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        prices = client.getPrices(fromSymbols, toSymbol, exchange);
        return 200;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (listener != null) {
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
        void finished(Prices prices);
    }
}