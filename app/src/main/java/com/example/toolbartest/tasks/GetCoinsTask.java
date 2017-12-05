package com.example.toolbartest.tasks;

import android.os.AsyncTask;

import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.cryptocompare.Client;

import java.util.ArrayList;

public class GetCoinsTask extends AsyncTask<Integer, Integer, Integer> {
    private Listener listener;
    private Client client;
    private ArrayList<Coin> coins;
    private String[] fromSymbols;
    private String toSymbol;

    public GetCoinsTask(Client client) {
        this.listener = null;
        this.client = client;
        this.coins = null;
        this.fromSymbols = null;
        this.toSymbol = null;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        coins = client.getCoins(fromSymbols, toSymbol);
        return 200;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (listener != null) {
            listener.finished(coins);
        }
    }

    public GetCoinsTask setFromSymbols(String[] fromSymbols) {
        this.fromSymbols = fromSymbols;
        return this;
    }

    public GetCoinsTask setToSymbol(String toSymbol) {
        this.toSymbol = toSymbol;
        return this;
    }

    public GetCoinsTask setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public interface Listener {
        void finished(ArrayList<Coin> coins);
    }
}