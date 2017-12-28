package com.example.coinkarasu.tasks.by_exchange;

import com.example.coinkarasu.api.cryptocompare.Client;
import com.example.coinkarasu.api.cryptocompare.data.Prices;

import java.util.concurrent.CountDownLatch;

public class GetCccaggPricesThread extends Thread {
    private CountDownLatch latch;
    private Prices prices;

    private Client client;
    private String[] fromSymbols;
    private String toSymbol;
    private String exchange;

    public GetCccaggPricesThread(Client client, String[] fromSymbols, String toSymbol, String exchange) {
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

    public GetCccaggPricesThread setLatch(CountDownLatch latch) {
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