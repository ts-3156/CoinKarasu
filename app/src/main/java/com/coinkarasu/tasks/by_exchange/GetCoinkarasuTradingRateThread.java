package com.coinkarasu.tasks.by_exchange;

import android.content.Context;

import com.coinkarasu.api.coincheck.data.Rate;
import com.coinkarasu.api.coinkarasu.Client;
import com.coinkarasu.tasks.CKThread;
import com.coinkarasu.utils.ApiKeyUtils;

import java.util.concurrent.CountDownLatch;

public class GetCoinkarasuTradingRateThread extends CKThread {
    private CountDownLatch latch;

    private Client client;
    private String fromSymbol;
    private String toSymbol;
    private Rate rate;

    public GetCoinkarasuTradingRateThread(Context context) {
        this.latch = null;

        this.client = new Client(context, ApiKeyUtils.getValidToken(context));
        this.fromSymbol = "BTC";
        this.toSymbol = "JPY";
    }

    @Override
    public void run() {
        super.run();

        rate = client.getTradingRate(fromSymbol, toSymbol);

        if (latch != null) {
            latch.countDown();
        }
    }

    public GetCoinkarasuTradingRateThread setLatch(CountDownLatch latch) {
        this.latch = latch;
        return this;
    }

    public Rate getRate() {
        return rate;
    }
}
