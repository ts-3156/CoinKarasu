package com.coinkarasu.tasks.by_exchange;

import android.content.Context;

import com.coinkarasu.api.coincheck.Client;
import com.coinkarasu.api.coincheck.data.Rate;
import com.coinkarasu.tasks.CKThread;

import java.util.concurrent.CountDownLatch;

public class GetCoincheckTradingRateThread extends CKThread {
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
        super.run();

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
