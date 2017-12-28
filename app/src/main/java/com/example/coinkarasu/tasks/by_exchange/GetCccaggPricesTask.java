package com.example.coinkarasu.tasks.by_exchange;

import android.content.Context;

import com.example.coinkarasu.activities.etc.CoinKind;
import com.example.coinkarasu.activities.etc.Exchange;
import com.example.coinkarasu.api.cryptocompare.data.Prices;
import com.example.coinkarasu.coins.PriceMultiFullCoin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetCccaggPricesTask extends GetPricesByExchangeTaskBase {
    private ArrayList<GetCccaggPricesThread> threads;
    private String[] fromSymbols;
    private String toSymbol;
    private String exchangeStr;
    private Context context;

    public GetCccaggPricesTask(Context context, Exchange exchange) {
        super(exchange, CoinKind.trading);
        this.threads = new ArrayList<>();
        this.fromSymbols = null;
        this.toSymbol = null;
        this.exchangeStr = null;
        this.context = context;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        publishProgress(0);

        for (int i = 0; i < fromSymbols.length; i += 20) {
            int index = i + 19;
            if (index >= fromSymbols.length) {
                index = fromSymbols.length - 1;
            }

            String[] target = Arrays.copyOfRange(fromSymbols, i, index + 1);
            threads.add(new GetCccaggPricesThread(context, target, toSymbol, exchangeStr));

            if (index >= fromSymbols.length) {
                break;
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(2);
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

    public GetCccaggPricesTask setFromSymbols(String[] fromSymbols) {
        this.fromSymbols = fromSymbols;
        return this;
    }

    public GetCccaggPricesTask setToSymbol(String toSymbol) {
        this.toSymbol = toSymbol;
        return this;
    }

    public GetCccaggPricesTask setExchange(String exchangeStr) {
        this.exchangeStr = exchangeStr;
        return this;
    }
}