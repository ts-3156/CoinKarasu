package com.example.coinkarasu.tasks;

import android.os.AsyncTask;

import com.example.coinkarasu.cryptocompare.Client;
import com.example.coinkarasu.cryptocompare.data.Price;

public class GetPriceTask extends AsyncTask<Integer, Integer, Integer> {
    private Listener listener;
    private Client client;
    private String fromSymbol;
    private String toSymbol;
    private String exchange;
    private Price price;

    public GetPriceTask(Client client) {
        this.listener = null;
        this.client = client;
        this.fromSymbol = null;
        this.toSymbol = null;
        this.exchange = null;
        this.price = null;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        publishProgress(0);
        price = client.getPrice(fromSymbol, toSymbol, exchange);
        return 200;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        if (listener != null) {
            listener.started(exchange, fromSymbol, toSymbol);
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (listener != null) {
            listener.finished(price);
        }
    }

    public GetPriceTask setFromSymbol(String fromSymbol) {
        this.fromSymbol = fromSymbol;
        return this;
    }

    public GetPriceTask setToSymbol(String toSymbol) {
        this.toSymbol = toSymbol;
        return this;
    }

    public GetPriceTask setExchange(String exchange) {
        this.exchange = exchange;
        return this;
    }

    public GetPriceTask setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public interface Listener {
        void started(String exchange, String fromSymbol, String toSymbol);

        void finished(Price price);
    }
}