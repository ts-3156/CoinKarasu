package com.example.coinkarasu.tasks.by_exchange;

import android.content.Context;

import com.example.coinkarasu.activities.etc.CoinKind;
import com.example.coinkarasu.activities.etc.Exchange;
import com.example.coinkarasu.api.coincheck.data.Rate;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetCoincheckTradingRatesTask extends GetPricesByExchangeTaskBase {
    private ArrayList<GetCoincheckTradingRateThread> threads;
    private Context context;

    protected GetCoincheckTradingRatesTask(Context context) {
        super(Exchange.coincheck, CoinKind.trading);
        this.context = context;
        this.threads = new ArrayList<>();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        publishProgress(0);

        threads.add(new GetCoincheckTradingRateThread(context, "sell"));
        threads.add(new GetCoincheckTradingRateThread(context, "buy"));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(threads.size());

        for (GetCoincheckTradingRateThread thread : threads) {
            thread.setLatch(latch);
            executor.submit(thread);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
        }

        executor.shutdown();

        return 200;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (listener != null) {
            double sum = 0.0;

            for (GetCoincheckTradingRateThread thread : threads) {
                sum += thread.getRate().value;
            }

            Rate rate = threads.get(0).getRate();
            ArrayList<Price> prices = new ArrayList<>();
            prices.add(new Price(Exchange.coincheck, CoinKind.trading, rate.fromSymbol, rate.toSymbol, sum / threads.size()));

            listener.finished(Exchange.coincheck, CoinKind.trading, prices);
        }
    }
}