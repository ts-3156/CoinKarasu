package com.coinkarasu.tasks;

import android.os.AsyncTask;

import com.coinkarasu.api.cryptocompare.Client;
import com.coinkarasu.api.cryptocompare.data.History;

import java.util.ArrayList;
import java.util.List;

public class GetHistoryTaskBase extends AsyncTask<Integer, Integer, List<History>> {
    Listener listener;
    Client client;
    String fromSymbol;
    String toSymbol;
    String exchange;

    public GetHistoryTaskBase(Client client) {
        this.listener = null;
        this.client = client;
        this.fromSymbol = null;
        this.toSymbol = null;
        this.exchange = "cccagg";
    }

    @Override
    protected List<History> doInBackground(Integer... params) {
        throw new RuntimeException("Stub");
    }

    @Override
    protected void onPostExecute(List<History> histories) {
        if (listener != null) {
            listener.finished(histories);
        }
    }

    public static GetHistoryTaskBase newInstance(Client client, String kind) {
        GetHistoryTaskBase instance;

        switch (kind) {
            case "hour":
                instance = new GetHistoryHourTask(client);
                break;
            case "day":
                instance = new GetHistoryDayTask(client);
                break;
            case "week":
                instance = new GetHistoryWeekTask(client);
                break;
            case "month":
                instance = new GetHistoryMonthTask(client);
                break;
            case "year":
                instance = new GetHistoryYearTask(client);
                break;
            default:
                instance = new GetHistoryHourTask(client);
        }

        return instance;
    }

    public static GetHistoryTaskBase newInstance(Client client, String kind, String exchange) {
        GetHistoryTaskBase instance;

        switch (kind) {
            case "day":
                instance = new GetHistoryDayTask(client, exchange);
                break;
            default:
                throw new RuntimeException("Invalid kind " + kind);
        }

        return instance;
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
        void finished(List<History> histories);
    }
}