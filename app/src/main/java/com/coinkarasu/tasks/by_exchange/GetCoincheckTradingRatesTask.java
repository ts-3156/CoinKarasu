package com.coinkarasu.tasks.by_exchange;

import android.content.Context;

import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.api.coincheck.data.Rate;
import com.coinkarasu.api.cryptocompare.data.Prices;
import com.coinkarasu.coins.PriceMultiFullCoin;
import com.coinkarasu.tasks.by_exchange.data.Price;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetCoincheckTradingRatesTask extends GetPricesByExchangeTaskBase {
    private ArrayList<Thread> threads;
    private Context context;

    protected GetCoincheckTradingRatesTask(Context context, CoinKind coinKind) {
        super(Exchange.coincheck, coinKind);
        this.context = context;
        this.threads = new ArrayList<>();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        publishProgress(0);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(3);

        threads.add(new GetCoincheckTradingRateThread(context, "sell").setLatch(latch));
        threads.add(new GetCoincheckTradingRateThread(context, "buy").setLatch(latch));
        threads.add(new GetCccaggPricesThread(context, new String[]{"BTC"}, "JPY", exchange.name()).setLatch(latch));


        for (Thread thread : threads) {
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
            Rate sellRate = ((GetCoincheckTradingRateThread) threads.get(0)).getRate();
            Rate buyRate = ((GetCoincheckTradingRateThread) threads.get(1)).getRate();
            if (sellRate == null || buyRate == null) {
                listener.finished(exchange, coinKind, null);
                return;
            }

            double avg = (sellRate.value + buyRate.value) / 2.0;
            Price price = new Price(exchange, coinKind, sellRate.fromSymbol, sellRate.toSymbol, avg);

            Prices prices = ((GetCccaggPricesThread) threads.get(2)).getPrices();
            if (!prices.getCoins().isEmpty()) {
                PriceMultiFullCoin coin = prices.getCoins().get(0);
                price.priceDiff = coin.getChange24Hour();
                price.trend = coin.getChangePct24Hour() / 100.0;
            }

            ArrayList<Price> pricesArray = new ArrayList<>();
            pricesArray.add(price);

            listener.finished(exchange, coinKind, pricesArray);
        }
    }
}