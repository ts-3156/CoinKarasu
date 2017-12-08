package com.example.toolbartest.tasks;

import android.os.AsyncTask;

import com.example.toolbartest.cryptocompare.Client;
import com.example.toolbartest.cryptocompare.data.History;

import java.util.ArrayList;

public class GetHistoryTaskBase extends AsyncTask<Integer, Integer, Integer> {
    Listener listener;
    Client client;
    ArrayList<History> histories;
    String fromSymbol;
    String toSymbol;

    public GetHistoryTaskBase(Client client) {
        this.listener = null;
        this.client = client;
        this.histories = null;
        this.fromSymbol = null;
        this.toSymbol = null;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        throw new RuntimeException("Stub");
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (listener != null) {
            listener.finished(histories);
        }
    }

    public GetHistoryTaskBase setFromSymbol(String fromSymbol) {
        this.fromSymbol = fromSymbol;
        return this;
    }

    public GetHistoryTaskBase setToSymbol(String toSymbol) {
        this.toSymbol = toSymbol;
        return this;
    }

    public GetHistoryTaskBase setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public interface Listener {
        void finished(ArrayList<History> histories);
    }
}