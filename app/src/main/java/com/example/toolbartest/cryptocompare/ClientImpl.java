package com.example.toolbartest.cryptocompare;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.example.toolbartest.cryptocompare.data.CoinList;
import com.example.toolbartest.cryptocompare.data.CoinListImpl;
import com.example.toolbartest.cryptocompare.data.Prices;
import com.example.toolbartest.cryptocompare.data.PricesImpl;
import com.example.toolbartest.cryptocompare.response.CoinListResponseImpl;
import com.example.toolbartest.tasks.FetchCoinListTask;

import org.json.JSONObject;

public class ClientImpl implements Client {
    private Activity activity;

    public ClientImpl(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void getCoinList(final String[] fromSymbols, final String toSymbol, final CoinListListener listener) {
        if (CoinListResponseImpl.cacheExists(activity)) {
            Log.d("CoinList cache hit", CoinListResponseImpl.lastModified(activity).toString());

            try {
                CoinList coinList = CoinListImpl.restoreFromCache(activity);
                coinList.setFromSymbols(fromSymbols);
                coinList.setToSymbol(toSymbol);

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
                    public void finished(JSONObject coinListResponse, JSONObject coinPricesResponse) {
                        CoinList coinList = CoinListImpl.buildByResponse(coinListResponse);
                        PricesImpl prices = PricesImpl.buildByResponse(coinPricesResponse);

                        coinList.setPrices(prices.getPrices());
                        coinList.setTrends(prices.getTrends());
                        coinList.setFromSymbols(fromSymbols);
                        coinList.setToSymbol(toSymbol);

                        if (listener != null) {
                            listener.finished(coinList);
                        }

                        coinList.saveToCache(activity);
                        prices.saveToCache(activity);
                    }
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
