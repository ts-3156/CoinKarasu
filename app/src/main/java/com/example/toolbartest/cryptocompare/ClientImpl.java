package com.example.toolbartest.cryptocompare;

import android.app.Activity;
import android.util.Log;

import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.cryptocompare.data.CoinList;
import com.example.toolbartest.cryptocompare.data.CoinListImpl;
import com.example.toolbartest.cryptocompare.data.CoinSnapshot;
import com.example.toolbartest.cryptocompare.data.CoinSnapshotImpl;
import com.example.toolbartest.cryptocompare.data.History;
import com.example.toolbartest.cryptocompare.data.HistoryImpl;
import com.example.toolbartest.cryptocompare.data.Prices;
import com.example.toolbartest.cryptocompare.data.PricesImpl;
import com.example.toolbartest.cryptocompare.request.BlockingRequest;
import com.example.toolbartest.cryptocompare.response.CoinListResponseImpl;
import com.example.toolbartest.cryptocompare.response.CoinSnapshotResponse;
import com.example.toolbartest.cryptocompare.response.CoinSnapshotResponseImpl;
import com.example.toolbartest.cryptocompare.response.HistoryResponse;
import com.example.toolbartest.cryptocompare.response.HistoryResponseImpl;
import com.example.toolbartest.cryptocompare.response.PricesResponseImpl;
import com.example.toolbartest.tasks.FetchCoinListThread;
import com.example.toolbartest.utils.StringHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
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
            long start = System.currentTimeMillis();
            coinList = CoinListImpl.restoreFromCache(activity);
            Log.d("CoinList", "" + (System.currentTimeMillis() - start) + " ms");
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
    public Prices getPrices(String[] fromSymbols, String toSymbol) {
        return getPrices(fromSymbols, toSymbol, "cccagg");
    }

    @Override
    public Prices getPrices(String[] fromSymbols, String toSymbol, String exchange) {
        String url = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms="
                + StringHelper.join(",", fromSymbols)
                + "&tsyms=" + toSymbol
                + "&e=" + exchange;
        Log.d("URL", url);
        JSONObject response = new BlockingRequest(activity, url).perform();
        return new PricesImpl(new PricesResponseImpl(response), exchange);
    }

    private ArrayList<History> getHistoryXxx(String kind, String fromSymbol, String toSymbol, int limit, int aggregate) {
        String url = "https://min-api.cryptocompare.com/data/histo" + kind +
                "?fsym=" + fromSymbol + "&tsym=" + toSymbol +
                "&limit=" + limit + "&aggregate=" + aggregate;
        Log.d("URL", url);

        JSONObject response = new BlockingRequest(activity, url).perform();
        HistoryResponse historyResponse = new HistoryResponseImpl(response, fromSymbol, toSymbol);

        JSONArray histories = historyResponse.getData();
        ArrayList<History> result = new ArrayList<>();

        try {
            for (int i = 0; i < histories.length(); i++) {
                result.add(new HistoryImpl(histories.getJSONObject(i), fromSymbol, toSymbol));
            }
        } catch (JSONException e) {
            Log.d("getHistoryMinute", e.getMessage());
        }

        return result;
    }

    @Override
    public ArrayList<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit, int aggregate) {
        ArrayList<History> records = getHistoryXxx("minute", fromSymbol, toSymbol, limit, 1);
        if (aggregate == 1) {
            return records;
        } else {
            return sampling(records, aggregate);
        }
    }

    @Override
    public ArrayList<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit) {
        return getHistoryMinute(fromSymbol, toSymbol, limit, 1);
    }

    @Override
    public ArrayList<History> getHistoryHour(String fromSymbol, String toSymbol, int limit, int aggregate) {
        ArrayList<History> records = getHistoryXxx("hour", fromSymbol, toSymbol, limit, 1);
        if (aggregate == 1) {
            return records;
        } else {
            return sampling(records, aggregate);
        }
    }

    @Override
    public ArrayList<History> getHistoryHour(String fromSymbol, String toSymbol, int limit) {
        return getHistoryHour(fromSymbol, toSymbol, limit, 1);
    }

    @Override
    public ArrayList<History> getHistoryDay(String fromSymbol, String toSymbol, int limit) {
        return getHistoryDay(fromSymbol, toSymbol, limit, 1);
    }

    @Override
    public ArrayList<History> getHistoryDay(String fromSymbol, String toSymbol, int limit, int aggregate) {
        ArrayList<History> records = getHistoryXxx("day", fromSymbol, toSymbol, limit, 1);
        if (aggregate == 1) {
            return records;
        } else {
            return sampling(records, aggregate);
        }
    }

    @Override
    public CoinSnapshot getCoinSnapshot(String fromSymbol, String toSymbol) {
        String url = "https://www.cryptocompare.com/api/data/coinsnapshot/?fsym=" + fromSymbol + "&tsym=" + toSymbol;
        Log.d("URL", url);

        JSONObject response = new BlockingRequest(activity, url).perform();
        CoinSnapshotResponse snapshotResponse = new CoinSnapshotResponseImpl(response, fromSymbol, toSymbol);
        return new CoinSnapshotImpl(snapshotResponse);
    }

    private ArrayList<History> sampling(ArrayList<History> records, int aggregate) {
        ArrayList<History> sampled = new ArrayList<>();
        int size = records.size();

        for (int i = 0; i < size; i++) {
            if (i != 0 && i != size - 1 && i % aggregate == 0) {
                sampled.add(records.get(i));
            }
        }

        Log.d("SAMPLING", "" + aggregate + ", " + records.size() + ", " + sampled.size());

        return sampled;
    }

}
