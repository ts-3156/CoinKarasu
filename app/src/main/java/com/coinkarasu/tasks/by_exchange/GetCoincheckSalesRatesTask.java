package com.coinkarasu.tasks.by_exchange;

import android.content.Context;

import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.api.coincheck.data.Rate;
import com.coinkarasu.tasks.by_exchange.data.Price;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetCoincheckSalesRatesTask extends GetPricesByExchangeTaskBase {
    private ArrayList<GetCoincheckSalesRateThread> threads;
    private ArrayList<GetCoinkarasuSalesRateThread> threads2;
    private String[] fromSymbols;
    private Context context;

    protected GetCoincheckSalesRatesTask(Context context) {
        super(Exchange.coincheck, CoinKind.sales);
        this.fromSymbols = context.getResources().getStringArray(Exchange.coincheck.salesSymbolsResId);
        this.context = context;
        this.threads = new ArrayList<>();
        this.threads2 = new ArrayList<>();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        publishProgress(0);

        for (String fromSymbol : fromSymbols) {
            threads.add(new GetCoincheckSalesRateThread(context, fromSymbol));
            threads2.add(new GetCoinkarasuSalesRateThread(context, fromSymbol));
        }

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(threads.size() + threads2.size());

        for (GetCoincheckSalesRateThread thread : threads) {
            thread.setLatch(latch);
            executor.submit(thread);
        }
        for (GetCoinkarasuSalesRateThread thread : threads2) {
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
            List<Price> prices = new ArrayList<>();

            for (GetCoincheckSalesRateThread thread : threads) {
                Rate rate = thread.getRate();
                if (rate == null) {
                    continue;
                }
                Price price = new Price(exchange, coinKind, rate.fromSymbol, rate.toSymbol, rate.value);

                for (GetCoinkarasuSalesRateThread t : threads2) {
                    Rate r = t.getRate();
                    if (r != null && r.value != 0.0 && r.fromSymbol.equals(rate.fromSymbol) && r.toSymbol.equals(rate.toSymbol)) {
                        price.priceDiff = price.price - r.value;
                        price.trend = price.priceDiff / r.value;
                        break;
                    }
                }

                prices.add(price);
            }

            listener.finished(exchange, coinKind, prices);
        }
    }
}