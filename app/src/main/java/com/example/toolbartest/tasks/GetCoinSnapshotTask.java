package com.example.toolbartest.tasks;

import android.os.AsyncTask;

import com.example.toolbartest.cryptocompare.Client;
import com.example.toolbartest.cryptocompare.data.CoinSnapshot;

public class GetCoinSnapshotTask extends AsyncTask<Integer, Integer, Integer> {
    Listener listener;
    Client client;
    CoinSnapshot snapshot;
    String fromSymbol;
    String toSymbol;

    public GetCoinSnapshotTask(Client client) {
        this.listener = null;
        this.client = client;
        this.snapshot = null;
        this.fromSymbol = null;
        this.toSymbol = null;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        snapshot = client.getCoinSnapshot(fromSymbol, toSymbol);
        return 200;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (listener != null) {
            listener.finished(snapshot);
        }
    }

    public GetCoinSnapshotTask setFromSymbol(String fromSymbol) {
        this.fromSymbol = fromSymbol;
        return this;
    }

    public GetCoinSnapshotTask setToSymbol(String toSymbol) {
        this.toSymbol = toSymbol;
        return this;
    }

    public GetCoinSnapshotTask setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public interface Listener {
        void finished(CoinSnapshot snapshot);
    }
}