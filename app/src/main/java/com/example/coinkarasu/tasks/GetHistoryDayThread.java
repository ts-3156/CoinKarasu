package com.example.coinkarasu.tasks;

import com.example.coinkarasu.cryptocompare.Client;
import com.example.coinkarasu.cryptocompare.data.History;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class GetHistoryDayThread extends Thread {
    private CountDownLatch latch;
    private ArrayList<History> histories;

    private Client client;
    private String fromSymbol;
    private String toSymbol;
    private String exchange;

    public GetHistoryDayThread(Client client, String fromSymbol, String toSymbol, String exchange) {
        latch = null;
        histories = null;

        this.client = client;
        this.fromSymbol = fromSymbol;
        this.toSymbol = toSymbol;
        this.exchange = exchange;
    }

    @Override
    public void run() {
        histories = client.getHistoryMinute(fromSymbol, toSymbol, 1440, 20, exchange); // 60 * 24

        if (latch != null) {
            latch.countDown();
        }
    }

    public GetHistoryDayThread setLatch(CountDownLatch latch) {
        this.latch = latch;
        return this;
    }

    public String getExchange() {
        return exchange;
    }

    public ArrayList<History> getHistories() {
        return histories;
    }
}