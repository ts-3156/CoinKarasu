package com.example.coinkarasu.tasks;

import android.os.AsyncTask;

import com.example.coinkarasu.cryptocompare.Client;
import com.example.coinkarasu.cryptocompare.data.Prices;

import java.util.Arrays;

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
        publishProgress(0);

        for (int i = 0; i < fromSymbols.length; i += 20) {
            int index = i + 19;
            if (index >= fromSymbols.length) {
                index = fromSymbols.length - 1;
            }

            String[] target = Arrays.copyOfRange(fromSymbols, i, index + 1);

            if (prices == null) {
                prices = client.getPrices(target, toSymbol, exchange);
            } else {
                prices.merge(client.getPrices(target, toSymbol, exchange));
            }

            if (index >= fromSymbols.length) {
                break;
            }
        }

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