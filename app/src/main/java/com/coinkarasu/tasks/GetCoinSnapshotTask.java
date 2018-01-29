package com.coinkarasu.tasks;

import android.os.AsyncTask;

import com.coinkarasu.api.cryptocompare.Client;
import com.coinkarasu.api.cryptocompare.data.CoinSnapshot;

public class GetCoinSnapshotTask extends AsyncTask<Integer, Integer, CoinSnapshot> {
    private Listener listener;
    private Client client;
    private String fromSymbol;
    private String toSymbol;

    public GetCoinSnapshotTask(Client client) {
        this.listener = null;
        this.client = client;
        this.fromSymbol = null;
        this.toSymbol = null;
    }

    @Override
    protected CoinSnapshot doInBackground(Integer... params) {
        return client.getCoinSnapshot(fromSymbol, toSymbol);
    }

    @Override
    protected void onPostExecute(CoinSnapshot result) {
        if (listener != null) {
            listener.finished(result);
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
