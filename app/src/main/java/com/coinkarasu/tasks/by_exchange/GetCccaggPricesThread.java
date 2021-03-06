package com.coinkarasu.tasks.by_exchange;

import android.content.Context;

import com.coinkarasu.api.cryptocompare.Client;
import com.coinkarasu.api.cryptocompare.ClientFactory;
import com.coinkarasu.api.cryptocompare.data.Prices;
import com.coinkarasu.tasks.CKThread;

import java.util.concurrent.CountDownLatch;

public class GetCccaggPricesThread extends CKThread {
    private CountDownLatch latch;
    private Prices prices;

    private Client client;
    private String[] fromSymbols;
    private String toSymbol;
    private String exchange;

    public GetCccaggPricesThread(Context context, String[] fromSymbols, String toSymbol, String exchange) {
        latch = null;
        prices = null;

        this.client = ClientFactory.getInstance(context);
        this.fromSymbols = fromSymbols;
        this.toSymbol = toSymbol;
        this.exchange = exchange;
    }

    @Override
    public void run() {
        super.run();

        prices = client.getPrices(fromSymbols, toSymbol, exchange);

        if (latch != null) {
            latch.countDown();
        }
    }

    public GetCccaggPricesThread setLatch(CountDownLatch latch) {
        this.latch = latch;
        return this;
    }

    public String[] getFromSymbols() {
        return fromSymbols;
    }

    public Prices getPrices() {
        return prices;
    }

    public String getExchange() {
        return exchange;
    }
}
