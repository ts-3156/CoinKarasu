package com.coinkarasu.tasks.by_exchange;

import android.content.Context;

import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.api.bitflyer.data.Board;
import com.coinkarasu.api.cryptocompare.data.Prices;
import com.coinkarasu.coins.PriceMultiFullCoin;
import com.coinkarasu.tasks.by_exchange.data.Price;
import com.coinkarasu.utils.CKLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetBitflyerTradingRatesTask extends GetPricesByExchangeTaskBase {
    private static final boolean DEBUG = true;
    private static final String TAG = "GetBitflyerTradingRatesTask";

    private List<Thread> threads;
    private Context context;

    protected GetBitflyerTradingRatesTask(Context context) {
        super(Exchange.bitflyer, CoinKind.none);
        this.context = context;
        this.threads = new ArrayList<>();
    }

    @Override
    protected List<Price> doInBackground(Integer... params) {
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

        List<Price> result = new ArrayList<>();

        Board board = ((GetBitflyerBoardThread) threads.get(0)).getBoard();
        if (board == null) {
            if (DEBUG) CKLog.w(TAG, "board is null");
            return result;
        }
        Price price = new Price(exchange, coinKind, "BTC", "JPY", board.getMidPrice());

        Prices prices = ((GetCccaggPricesThread) threads.get(1)).getPrices();
        if (prices == null || prices.getCoins() == null || prices.getCoins().isEmpty()) {
            if (DEBUG) CKLog.w(TAG, "prices is null");
            return result;
        }

        PriceMultiFullCoin coin = prices.getCoins().get(0);
        price.priceDiff = coin.getChange24Hour();
        price.trend = coin.getChangePct24Hour() / 100.0;

        result.add(price);

        return result;
    }

    @Override
    protected void onPostExecute(List<Price> prices) {
        if (listener != null) {
            listener.finished(exchange, coinKind, prices);
        }
        context = null;
    }
}