package com.coinkarasu.tasks.by_exchange;

import android.content.Context;

import com.coinkarasu.api.coincheck.Client;
import com.coinkarasu.api.coincheck.data.Rate;

import java.util.concurrent.CountDownLatch;

public class GetCoincheckSalesRateThread extends Thread {
    private CountDownLatch latch;

    private Client client;
    private String fromSymbol;
    private String toSymbol;
    private Rate rate;

    public GetCoincheckSalesRateThread(Context context, String fromSymbol) {
        this.latch = null;

        this.client = new Client(context);
        this.fromSymbol = fromSymbol;
        this.toSymbol = "JPY";
    }

    @Override
    public void run() {
        rate = client.getSalesRate(fromSymbol, toSymbol);

        if (latch != null) {
            latch.countDown();
        }
    }

    public GetCoincheckSalesRateThread setLatch(CountDownLatch latch) {
        this.latch = latch;
        return this;
    }

    public Rate getRate() {
        return rate;
    }
}