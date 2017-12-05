package com.example.toolbartest.cryptocompare;

import android.app.Activity;
import android.util.Log;

import com.example.toolbartest.coins.CoinList;
import com.example.toolbartest.coins.CoinListImpl;
import com.example.toolbartest.tasks.FetchCoinListTask;
import com.example.toolbartest.utils.StringHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class ClientImpl implements Client {
    private Activity activity;

    public ClientImpl(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void getCoinList(final String[] fromSymbols, final String toSymbol, final CoinListListener listener) {
//        if (CoinListResponseImpl.cacheExists(activity)) {
//            Log.d("CoinList cache hit", CoinListResponseImpl.lastModified(activity).toString());
//            CoinList coinList = CoinListImpl.builder().setActivity(activity).build();
//            if (listener != null) {
//                listener.finished(coinList);
//            }
//            new FetchCoinListTask(activity).setFromSymbols(fromSymbols).setToSymbol(toSymbol).execute();
//            return;
//        }

        Log.d("CoinList cache", "Not found");

        new FetchCoinListTask(activity)
                .setFromSymbols(fromSymbols)
                .setToSymbol(toSymbol)
                .setListener(new FetchCoinListTask.Listener() {
                    @Override
                    public void finished(JSONObject coinListResponse, JSONObject coinPricesResponse) {
                        CoinList coinList = new CoinListImpl(new CoinListResponseImpl(coinListResponse));
                        Prices prices = Prices.buildByResponse(coinPricesResponse);

                        coinList.setPrices(prices.getPrices());
                        coinList.setTrends(prices.getTrends());
                        coinList.setFromSymbols(fromSymbols);
                        coinList.setToSymbol(toSymbol);

                        if (listener != null) {
                            listener.finished(coinList);
                        }

                        coinList.saveToFile(activity);
                    }
                }).execute();
    }
}
