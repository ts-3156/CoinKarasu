package com.coinkarasu.tasks.by_exchange;

import android.content.Context;

import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.api.coincheck.data.Rate;
import com.coinkarasu.tasks.by_exchange.data.Price;
import com.coinkarasu.utils.CKLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetCoincheckTradingRatesTask extends GetPricesByExchangeTaskBase {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "GetCoincheckTradingRatesTask";

    private List<Thread> threads;
    private Context context;
    private boolean hasWarning;

    protected GetCoincheckTradingRatesTask(Context context, CoinKind coinKind) {
        super(Exchange.coincheck, coinKind);
        this.context = context;
        this.threads = new ArrayList<>();
        this.hasWarning = false;
    }

    @Override
    protected List<Price> doInBackground(Integer... params) {
        publishProgress(0);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(3);

        threads.add(new GetCoincheckTradingRateThread(context, "sell").setLatch(latch));
        threads.add(new GetCoincheckTradingRateThread(context, "buy").setLatch(latch));
        threads.add(new GetCoinkarasuTradingRateThread(context).setLatch(latch));
//        threads.add(new GetCccaggPricesThread(context, new String[]{"BTC"}, "JPY", exchange.name()).setLatch(latch));


        for (Thread thread : threads) {
            executor.submit(thread);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
        }

        executor.shutdown();

        List<Price> result = new ArrayList<>();

        Rate sellRate = ((GetCoincheckTradingRateThread) threads.get(0)).getRate();
        Rate buyRate = ((GetCoincheckTradingRateThread) threads.get(1)).getRate();
        if (sellRate == null || buyRate == null) {
            if (DEBUG) CKLog.w(TAG, "sellRate is null or buyRate is null");
            return result;
        }

        double avg = (sellRate.value + buyRate.value) / 2.0;
        Price price = new Price(exchange, coinKind, sellRate.fromSymbol, sellRate.toSymbol, avg);

//            Prices prices = ((GetCccaggPricesThread) threads.get(2)).getPrices();
//            if (!prices.getCoins().isEmpty()) {
//                PriceMultiFullCoin coin = prices.getCoins().get(0);
//                price.priceDiff = coin.getChange24Hour();
//                price.trend = coin.getChangePct24Hour() / 100.0;
//            }

        Rate rate = ((GetCoinkarasuTradingRateThread) threads.get(2)).getRate();
        if (rate == null) {
            if (DEBUG) CKLog.w(TAG, "rate is null");
            hasWarning = true;
        } else {
            if (rate.value != 0.0) {
                price.priceDiff = price.price - rate.value;
                price.trend = price.priceDiff / rate.value;
            }
        }

        result.add(price);

        return result;
    }

    @Override
    protected void onPostExecute(List<Price> prices) {
        if (listener != null) {
            listener.finished(exchange, coinKind, prices, hasWarning);
        }
        context = null;
    }
}
