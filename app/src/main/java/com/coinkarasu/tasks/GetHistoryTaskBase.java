package com.coinkarasu.tasks;

import android.os.AsyncTask;

import com.coinkarasu.activities.etc.HistoricalPriceKind;
import com.coinkarasu.api.cryptocompare.CacheMode;
import com.coinkarasu.api.cryptocompare.Client;
import com.coinkarasu.api.cryptocompare.data.History;

import java.util.List;

public abstract class GetHistoryTaskBase extends AsyncTask<Integer, Integer, List<History>> {
    Listener listener;
    Client client;
    String fromSymbol;
    String toSymbol;
    String exchange;
    int cacheMode;

    public GetHistoryTaskBase(Client client) {
        this.listener = null;
        this.client = client;
        this.fromSymbol = null;
        this.toSymbol = null;
        this.exchange = "cccagg";
        this.cacheMode = CacheMode.NORMAL;
    }

    @Override
    protected abstract List<History> doInBackground(Integer... params);

    @Override
    protected void onPostExecute(List<History> histories) {
        if (listener != null) {
            listener.finished(histories);
        }
    }

    public static GetHistoryTaskBase newInstance(Client client, HistoricalPriceKind kind) {
        GetHistoryTaskBase instance;

        switch (kind) {
            case hour:
                instance = new GetHistoryHourTask(client);
                break;
            case day:
                instance = new GetHistoryDayTask(client);
                break;
            case week:
                instance = new GetHistoryWeekTask(client);
                break;
            case month:
                instance = new GetHistoryMonthTask(client);
                break;
            case year:
                instance = new GetHistoryYearTask(client);
                break;
            default:
                instance = new GetHistoryHourTask(client);
        }

        return instance;
    }

    public static GetHistoryTaskBase newInstance(Client client, HistoricalPriceKind kind, String exchange) {
        GetHistoryTaskBase instance;

        switch (kind) {
            case day:
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

    public GetHistoryTaskBase setCacheMode(int cacheMode) {
        this.cacheMode = cacheMode;
        return this;
    }

    public GetHistoryTaskBase setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public String getFromSymbol() {
        return fromSymbol;
    }

    public String getToSymbol() {
        return toSymbol;
    }

    public interface Listener {
        void finished(List<History> histories);
    }
}