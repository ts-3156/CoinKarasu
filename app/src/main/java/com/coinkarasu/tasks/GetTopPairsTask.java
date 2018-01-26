package com.coinkarasu.tasks;

import android.os.AsyncTask;

import com.coinkarasu.api.cryptocompare.Client;
import com.coinkarasu.api.cryptocompare.data.TopPairs;

public class GetTopPairsTask extends AsyncTask<Void, Void, TopPairs> {
    private Listener listener;
    private Client client;
    private String fromSymbol;

    public GetTopPairsTask(Client client) {
        this.listener = null;
        this.client = client;
        this.fromSymbol = null;
    }

    @Override
    protected TopPairs doInBackground(Void... params) {
        return client.getTopPairs(fromSymbol);
    }

    @Override
    protected void onPostExecute(TopPairs topPairs) {
        if (listener != null) {
            listener.finished(topPairs);
        }
    }

    public GetTopPairsTask setFromSymbol(String fromSymbol) {
        this.fromSymbol = fromSymbol;
        return this;
    }

    public GetTopPairsTask setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public interface Listener {
        void finished(TopPairs topPairs);
    }
}
