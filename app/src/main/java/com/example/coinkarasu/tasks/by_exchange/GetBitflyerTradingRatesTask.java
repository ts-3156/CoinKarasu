package com.example.coinkarasu.tasks.by_exchange;

import android.content.Context;

import com.example.coinkarasu.activities.etc.CoinKind;
import com.example.coinkarasu.activities.etc.Exchange;
import com.example.coinkarasu.api.bitflyer.data.Board;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetBitflyerTradingRatesTask extends GetPricesByExchangeTaskBase {
    private ArrayList<GetBitflyerBoardThread> threads;
    private Context context;

    protected GetBitflyerTradingRatesTask(Context context) {
        super(Exchange.bitflyer, CoinKind.trading);
        this.context = context;
        this.threads = new ArrayList<>();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        publishProgress(0);

        threads.add(new GetBitflyerBoardThread(context));

        ExecutorService executor = Executors.newFixedThreadPool(1);
        CountDownLatch latch = new CountDownLatch(threads.size());

        for (GetBitflyerBoardThread thread : threads) {
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
            Board board = threads.get(0).getBoard();

            ArrayList<Price> prices = new ArrayList<>();
            prices.add(new Price(Exchange.bitflyer, CoinKind.trading, "BTC", "JPY", board.getMidPrice()));

            listener.finished(Exchange.bitflyer, CoinKind.trading, prices);
        }
    }
}