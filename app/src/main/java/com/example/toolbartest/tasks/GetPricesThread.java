package com.example.toolbartest.tasks;

import com.example.toolbartest.cryptocompare.Client;
import com.example.toolbartest.cryptocompare.data.Prices;

import java.util.concurrent.CountDownLatch;

public class GetPricesThread extends Thread {
    private CountDownLatch latch;
    private Prices prices;

    private Client client;
    private String[] fromSymbols;
    private String toSymbol;
    private String exchange;

    public GetPricesThread(Client client, String[] fromSymbols, String toSymbol, String exchange) {
        latch = null;
        prices = null;

        this.client = client;
        this.fromSymbols = fromSymbols;
        this.toSymbol = toSymbol;
        this.exchange = exchange;
    }

    @Override
    public void run() {
        prices = client.getPrices(fromSymbols, toSymbol, exchange);

        if (latch != null) {
            latch.countDown();
        }
    }

    public GetPricesThread setLatch(CountDownLatch latch) {
        this.latch = latch;
        return this;
    }

    public Prices getPrices() {
        return prices;
    }

    public String getExchange() {
        return exchange;
    }
}