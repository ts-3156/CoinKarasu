package com.example.coinkarasu.tasks;

import android.content.Context;

import com.example.coinkarasu.api.coincheck.Client;
import com.example.coinkarasu.api.coincheck.data.Rate;

import java.util.concurrent.CountDownLatch;

public class GetCoincheckTradingRateThread extends Thread {
    private CountDownLatch latch;

    private Client client;
    private String orderType;
    private Rate rate;

    public GetCoincheckTradingRateThread(Context context, String orderType) {
        this.latch = null;

        this.client = new Client(context);
        this.orderType = orderType;
    }

    @Override
    public void run() {
        rate = client.getTradingRate(orderType);

        if (latch != null) {
            latch.countDown();
        }
    }

    public GetCoincheckTradingRateThread setLatch(CountDownLatch latch) {
        this.latch = latch;
        return this;
    }

    public Rate getRate() {
        return rate;
    }
}