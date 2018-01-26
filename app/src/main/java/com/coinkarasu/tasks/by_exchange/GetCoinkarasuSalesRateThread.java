package com.coinkarasu.tasks.by_exchange;

import android.content.Context;

import com.coinkarasu.api.coincheck.data.Rate;
import com.coinkarasu.api.coinkarasu.Client;
import com.coinkarasu.utils.ApiKeyUtils;
import com.coinkarasu.utils.Token;

import java.util.concurrent.CountDownLatch;

public class GetCoinkarasuSalesRateThread extends Thread {
    private CountDownLatch latch;

    private Client client;
    private String fromSymbol;
    private String toSymbol;
    private Rate rate;

    public GetCoinkarasuSalesRateThread(Context context, String fromSymbol) {
        this.latch = null;

        Token token =
                ApiKeyUtils.exists(context) ? ApiKeyUtils.get(context) : ApiKeyUtils.dummy();
        this.client = new Client(context, token.getKey(), token.getSecret());

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

    public GetCoinkarasuSalesRateThread setLatch(CountDownLatch latch) {
        this.latch = latch;
        return this;
    }

    public Rate getRate() {
        return rate;
    }
}
