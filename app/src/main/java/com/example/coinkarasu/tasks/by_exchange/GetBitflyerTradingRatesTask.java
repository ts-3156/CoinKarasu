package com.example.coinkarasu.tasks.by_exchange;

import android.content.Context;

import com.example.coinkarasu.activities.etc.CoinKind;
import com.example.coinkarasu.activities.etc.Exchange;
import com.example.coinkarasu.api.bitflyer.data.Board;
import com.example.coinkarasu.coins.PriceMultiFullCoin;
import com.example.coinkarasu.tasks.by_exchange.data.Price;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetBitflyerTradingRatesTask extends GetPricesByExchangeTaskBase {
    private ArrayList<Thread> threads;
    private Context context;

    protected GetBitflyerTradingRatesTask(Context context) {
        super(Exchange.bitflyer, CoinKind.none);
        this.context = context;
        this.threads = new ArrayList<>();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        publishProgress(0);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        threads.add(new GetBitflyerBoardThread(context).setLatch(latch));
        threads.add(new GetCccaggPricesThread(context, new String[]{"BTC"}, "JPY", Exchange.bitflyer.name()).setLatch(latch));

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
            Board board = ((GetBitflyerBoardThread) threads.get(0)).getBoard();
            PriceMultiFullCoin coin = ((GetCccaggPricesThread) threads.get(1)).getPrices().getCoins().get(0);

            Price price = new Price(exchange, coinKind, "BTC", "JPY", board.getMidPrice());
            price.priceDiff = coin.getChange24Hour();
            price.trend = coin.getChangePct24Hour() / 100.0;

            ArrayList<Price> pricesArray = new ArrayList<>();
            pricesArray.add(price);

            listener.finished(exchange, coinKind, pricesArray);
        }
    }
}