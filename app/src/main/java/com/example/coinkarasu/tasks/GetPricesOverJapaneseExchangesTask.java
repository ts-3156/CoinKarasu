package com.example.coinkarasu.tasks;

import android.os.AsyncTask;

import com.example.coinkarasu.cryptocompare.Client;
import com.example.coinkarasu.cryptocompare.data.Prices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class GetPricesOverJapaneseExchangesTask extends AsyncTask<Integer, Integer, Integer> {
    private static final String[] BITFLYER_SYMBOLS = {"BTC"};
    private static final String[] COINCHECK_SYMBOLS = {"BTC"};
    private static final String[] ZAIF_SYMBOLS = {"BTC", "XEM", "MONA", "BCH", "ETH"};

    public static final String EXCHANGE_BITFLYER = "bitflyer";
    public static final String EXCHANGE_COINCHECK = "coincheck";
    public static final String EXCHANGE_ZAIF = "zaif";

    private static final String TO_SYMBOL = "JPY";

    private Listener listener;
    private Client client;
    private ArrayList<GetPricesThread> threads;

    public GetPricesOverJapaneseExchangesTask(Client client) {
        this.listener = null;
        this.client = client;
        this.threads = new ArrayList<>(3);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        CountDownLatch latch = new CountDownLatch(3);

        threads.add(new GetPricesThread(client, BITFLYER_SYMBOLS, TO_SYMBOL, EXCHANGE_BITFLYER));
        threads.add(new GetPricesThread(client, COINCHECK_SYMBOLS, TO_SYMBOL, EXCHANGE_COINCHECK));
        threads.add(new GetPricesThread(client, ZAIF_SYMBOLS, TO_SYMBOL, EXCHANGE_ZAIF));

        for (GetPricesThread thread : threads) {
            thread.setLatch(latch).start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
        }

        return 200;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (listener != null) {
            HashMap<String, Prices> map = new HashMap<>();

            for (GetPricesThread thread : threads) {
                map.put(thread.getExchange(), thread.getPrices());
            }

            listener.finished(map);
        }
    }

    public GetPricesOverJapaneseExchangesTask setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public interface Listener {
        void finished(HashMap<String, Prices> map);
    }
}