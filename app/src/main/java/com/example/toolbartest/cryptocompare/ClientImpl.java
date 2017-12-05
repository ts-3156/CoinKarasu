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
    public ArrayList<Coin> getCoins(String[] fromSymbols, String toSymbol) {
        String url = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=" + StringHelper.join(",", fromSymbols) + "&tsyms=" + toSymbol;
        JSONObject response = new BlockingRequest(activity, url).perform();
        Prices prices = new PricesImpl(new PricesResponseImpl(response));

        if (coinList == null) {
            try {
                latch.await();
            } catch (InterruptedException e) {
            }
        }

        ArrayList<Coin> coins = coinList.collectCoins(fromSymbols, toSymbol);
        prices.setPriceAndTrendToCoins(coins);

        return coins;
    }

    @Override
    public void getCoins(final String[] fromSymbols, final String toSymbol, final CoinsListener listener) {
        getPrices(fromSymbols, toSymbol, new PricesListener() {
            @Override
            public void finished(Prices prices) {
                if (coinList == null) {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                    }
                }

                if (listener != null) {
                    ArrayList<Coin> coins = coinList.collectCoins(fromSymbols, toSymbol);
                    for (Coin coin : coins) {
                        prices.setPriceAndTrendToCoin(coin);
                    }
                    listener.finished(coins);
                }
            }
        });
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
    public void getCoinList(final String[] fromSymbols, final String toSymbol, final CoinListListener listener) {
        if (CoinListResponseImpl.cacheExists(activity)) {
            Log.d("CoinList cache hit", CoinListResponseImpl.lastModified(activity).toString());

            try {
                CoinList coinList = CoinListImpl.restoreFromCache(activity);

                Prices prices = PricesImpl.restoreFromCache(activity);
                coinList.setPrices(prices.getPrices());
                coinList.setTrends(prices.getTrends());

                if (listener != null) {
                    listener.finished(coinList);
                }

                return;
            } catch (Exception e) {
                Log.d("getCoinList", e.getMessage());
            }
        }

        Log.d("CoinList cache", "Not found");

        new FetchCoinListTask(activity)
                .setFromSymbols(fromSymbols)
                .setToSymbol(toSymbol)
                .setListener(new FetchCoinListTask.Listener() {
                    @Override
                    public void finished(JSONObject coinListResponse, JSONObject pricesResponse) {
                        CoinList coinList = CoinListImpl.buildByResponse(coinListResponse);
                        PricesImpl prices = PricesImpl.buildByResponse(pricesResponse);

                        coinList.setPrices(prices.getPrices());
                        coinList.setTrends(prices.getTrends());

                        if (listener != null) {
                            listener.finished(coinList);
                        }

                        coinList.saveToCache(activity);
                        prices.saveToCache(activity);
                    }
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public Prices getPrices(final String[] fromSymbols, final String toSymbol) {
        String url = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=" + StringHelper.join(",", fromSymbols) + "&tsyms=" + toSymbol;
        JSONObject response = new BlockingRequest(activity, url).perform();
        return new PricesImpl(new PricesResponseImpl(response));
    }

    @Override
    public void getPrices(final String[] fromSymbols, final String toSymbol, final PricesListener listener) {
        new FetchPricesTask(activity)
                .setFromSymbols(fromSymbols)
                .setToSymbol(toSymbol)
                .setListener(new FetchPricesTask.Listener() {
                    @Override
                    public void finished(JSONObject response) {
                        Prices prices = new PricesImpl(new PricesResponseImpl(response));
                        if (listener != null) {
                            listener.finished(prices);
                        }
                    }
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
