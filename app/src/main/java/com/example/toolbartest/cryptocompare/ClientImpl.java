package com.example.toolbartest.cryptocompare;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.cryptocompare.data.CoinList;
import com.example.toolbartest.cryptocompare.data.CoinListImpl;
import com.example.toolbartest.cryptocompare.data.Prices;
import com.example.toolbartest.cryptocompare.data.PricesImpl;
import com.example.toolbartest.cryptocompare.request.BlockingRequest;
import com.example.toolbartest.cryptocompare.response.CoinListResponseImpl;
import com.example.toolbartest.cryptocompare.response.PricesResponseImpl;
import com.example.toolbartest.tasks.FetchCoinListTask;
import com.example.toolbartest.tasks.FetchCoinListThread;
import com.example.toolbartest.tasks.FetchPricesTask;
import com.example.toolbartest.utils.StringHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class ClientImpl implements Client {
    private Activity activity;
    private CoinList coinList;
    private CountDownLatch latch;

    public ClientImpl(Activity activity) {
        this.activity = activity;
        this.coinList = null;
        initializeCoinList();
    }

    private void initializeCoinList() {
        if (CoinListResponseImpl.cacheExists(activity)) {
            coinList = CoinListImpl.restoreFromCache(activity);
        } else {
            latch = new CountDownLatch(1);
            new FetchCoinListThread(activity)
                    .setLatch(latch)
                    .setListener(new FetchCoinListThread.Listener() {
                        @Override
                        public void finished(JSONObject response) {
                            coinList = CoinListImpl.buildByResponse(response);
                        }
                    }).start();
        }
    }

    @Override
    public ArrayList<Coin> collectCoins(String[] fromSymbols, String toSymbol) {
        if (coinList == null) {
            try {
                latch.await();
            } catch (InterruptedException e) {
            }
        }

        return coinList.collectCoins(fromSymbols, toSymbol);
    }

    @Override
    public Prices getPrices(final String[] fromSymbols, final String toSymbol) {
        String url = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=" + StringHelper.join(",", fromSymbols) + "&tsyms=" + toSymbol;
        JSONObject response = new BlockingRequest(activity, url).perform();
        return new PricesImpl(new PricesResponseImpl(response));
    }
}
