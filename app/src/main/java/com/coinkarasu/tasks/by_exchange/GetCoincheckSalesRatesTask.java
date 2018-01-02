package com.coinkarasu.tasks.by_exchange;

import android.content.Context;

import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.api.coincheck.data.Rate;
import com.coinkarasu.tasks.by_exchange.data.Price;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetCoincheckSalesRatesTask extends GetPricesByExchangeTaskBase {
    private ArrayList<GetCoincheckSalesRateThread> threads;
    private String[] fromSymbols;
    private Context context;

    protected GetCoincheckSalesRatesTask(Context context) {
        super(Exchange.coincheck, CoinKind.sales);
        this.fromSymbols = context.getResources().getStringArray(Exchange.coincheck.salesSymbolsResId);
        this.context = context;
        this.threads = new ArrayList<>();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        publishProgress(0);

        for (String fromSymbol : fromSymbols) {
            threads.add(new GetCoincheckSalesRateThread(context, fromSymbol));

        }

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(threads.size());

        for (GetCoincheckSalesRateThread thread : threads) {
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
            ArrayList<Price> prices = new ArrayList<>();

            for (GetCoincheckSalesRateThread thread : threads) {
                Rate rate = thread.getRate();
                if (rate != null) {
                    prices.add(new Price(exchange, coinKind, rate.fromSymbol, rate.toSymbol, rate.value));
                }
            }

            listener.finished(exchange, coinKind, prices);
        }
    }
}