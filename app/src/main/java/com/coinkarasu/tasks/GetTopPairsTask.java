package com.coinkarasu.tasks;

import android.os.AsyncTask;

import com.coinkarasu.api.cryptocompare.Client;
import com.coinkarasu.api.cryptocompare.data.TopPair;

import java.util.ArrayList;
import java.util.List;

public class GetTopPairsTask extends AsyncTask<Integer, Integer, Integer> {
    private Listener listener;
    private Client client;
    private List<TopPair> topPairs;
    private String fromSymbol;

    public GetTopPairsTask(Client client) {
        this.listener = null;
        this.client = client;
        this.topPairs = null;
        this.fromSymbol = null;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        topPairs = client.getTopPairs(fromSymbol);
        return 200;
    }

    @Override
    protected void onPostExecute(Integer integer) {
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
        void finished(List<TopPair> topPairs);
    }
}