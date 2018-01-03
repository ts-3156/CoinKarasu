package com.coinkarasu.tasks.by_exchange;

import android.content.Context;

import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.api.cryptocompare.data.Prices;
import com.coinkarasu.coins.PriceMultiFullCoin;
import com.coinkarasu.tasks.by_exchange.data.Price;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetZaifTradingRatesTask extends GetPricesByExchangeTaskBase {
    private ArrayList<GetCccaggPricesThread> threads;
    private String[] fromSymbols;
    private String toSymbol;
    private Context context;

    public GetZaifTradingRatesTask(Context context, CoinKind coinKind) {
        super(Exchange.zaif, CoinKind.none);
        this.threads = new ArrayList<>();
        this.fromSymbols = context.getResources().getStringArray(exchange.tradingSymbolsResId);
        this.toSymbol = "JPY";
        this.context = context;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        publishProgress(0);

        threads.add(new GetCccaggPricesThread(context, fromSymbols, toSymbol, exchange.name()));

        ExecutorService executor = Executors.newFixedThreadPool(1);
        CountDownLatch latch = new CountDownLatch(threads.size());

        for (GetCccaggPricesThread thread : threads) {
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
            ArrayList<Price> pricesArray = new ArrayList<>();

            for (GetCccaggPricesThread thread : threads) {
                Prices prices = thread.getPrices();

                for (PriceMultiFullCoin coin : prices.getCoins()) {
                    Price price = new Price(exchange, coinKind, coin.getFromSymbol(), coin.getToSymbol(), coin.getPrice());
                    price.priceDiff = coin.getChange24Hour();
                    price.trend = coin.getChangePct24Hour() / 100.0;
                    pricesArray.add(price);
                }
            }

            listener.finished(exchange, coinKind, pricesArray);
        }
    }
}