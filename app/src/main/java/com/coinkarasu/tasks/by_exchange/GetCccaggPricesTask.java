package com.coinkarasu.tasks.by_exchange;

import android.content.Context;

import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.api.cryptocompare.data.Prices;
import com.coinkarasu.coins.PriceMultiFullCoin;
import com.coinkarasu.tasks.by_exchange.data.Price;
import com.coinkarasu.utils.CKLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetCccaggPricesTask extends GetPricesByExchangeTaskBase {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "GetCccaggPricesTask";

    private List<GetCccaggPricesThread> threads;
    private String[] fromSymbols;
    private String toSymbol;
    private String exchangeStr;
    private Context context;
    private boolean hasWarning;

    public GetCccaggPricesTask(Context context, Exchange exchange) {
        super(exchange, CoinKind.none);
        this.threads = new ArrayList<>();
        this.fromSymbols = null;
        this.toSymbol = null;
        this.exchangeStr = null;
        this.context = context;
        this.hasWarning = false;
    }

    @Override
    protected List<Price> doInBackground(Integer... params) {
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

        List<Price> result = new ArrayList<>();

        for (GetCccaggPricesThread thread : threads) {
            Prices prices = thread.getPrices();
            if (prices == null || prices.getCoins() == null || prices.getCoins().isEmpty()) {
                if (DEBUG) CKLog.w(TAG, "prices is blank " + exchangeStr + " "
                        + Arrays.toString(thread.getFromSymbols()) + " " + toSymbol);
                hasWarning = true;
                continue;
            }

            for (PriceMultiFullCoin coin : prices.getCoins()) {
                Price price = new Price(exchange, coinKind, coin.getFromSymbol(), coin.getToSymbol(), coin.getPrice());
                price.priceDiff = coin.getChange24Hour();
                price.trend = coin.getChangePct24Hour() / 100.0;
                result.add(price);
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(List<Price> prices) {
        if (listener != null) {
            listener.finished(exchange, coinKind, prices, hasWarning);
        }
        context = null;
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
