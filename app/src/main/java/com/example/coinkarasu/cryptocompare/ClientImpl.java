package com.example.coinkarasu.cryptocompare;

import android.app.Activity;
import android.util.Log;

import com.example.coinkarasu.coins.PriceMultiFullCoin;
import com.example.coinkarasu.cryptocompare.data.CoinList;
import com.example.coinkarasu.cryptocompare.data.CoinSnapshot;
import com.example.coinkarasu.cryptocompare.data.CoinSnapshotImpl;
import com.example.coinkarasu.cryptocompare.data.History;
import com.example.coinkarasu.cryptocompare.data.HistoryImpl;
import com.example.coinkarasu.cryptocompare.data.Price;
import com.example.coinkarasu.cryptocompare.data.PriceImpl;
import com.example.coinkarasu.cryptocompare.data.Prices;
import com.example.coinkarasu.cryptocompare.data.PricesImpl;
import com.example.coinkarasu.cryptocompare.data.TopPairs;
import com.example.coinkarasu.cryptocompare.data.TopPairsImpl;
import com.example.coinkarasu.cryptocompare.request.BlockingRequest;
import com.example.coinkarasu.cryptocompare.response.CoinListResponseImpl;
import com.example.coinkarasu.cryptocompare.response.CoinSnapshotResponse;
import com.example.coinkarasu.cryptocompare.response.CoinSnapshotResponseImpl;
import com.example.coinkarasu.cryptocompare.response.HistoryResponse;
import com.example.coinkarasu.cryptocompare.response.HistoryResponseImpl;
import com.example.coinkarasu.cryptocompare.response.PricesResponseImpl;
import com.example.coinkarasu.cryptocompare.response.TopPairsResponse;
import com.example.coinkarasu.cryptocompare.response.TopPairsResponseImpl;
import com.example.coinkarasu.utils.StringHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ClientImpl implements Client {
    private Activity activity;

    public ClientImpl(Activity activity) {
        this.activity = activity;
    }

    private void initializeCoinList() {
        if (CoinListResponseImpl.cacheExists(activity)) {
//            long start = System.currentTimeMillis();
//            coinList = CoinListImpl.restoreFromCache(activity);
//            Log.d("RestoreCoinList", (System.currentTimeMillis() - start) + " ms");
        } else {
//            latch = new CountDownLatch(1);
//            new FetchCoinListThread(activity)
//                    .setLatch(latch)
//                    .setListener(new FetchCoinListThread.Listener() {
//                        @Override
//                        public void finished(JSONObject response) {
//                            coinList = CoinListImpl.buildByResponse(response);
//                        }
//                    }).start();
        }
    }

    @Override
    public Price getPrice(String fromSymbol, String toSymbol, String exchange) {
        String url = "https://min-api.cryptocompare.com/data/pricemultifull?"
                + "&fsyms=" + fromSymbol
                + "&tsyms=" + toSymbol
                + "&e=" + exchange;
        Log.d("URL", url);

        JSONObject response = new BlockingRequest(activity, url).perform();
        return new PriceImpl(new PricesResponseImpl(response), exchange);
    }

    @Override
    public Prices getPrices(String[] fromSymbols, String toSymbol) {
        return getPrices(fromSymbols, toSymbol, "cccagg");
    }

    @Override
    public Prices getPrices(String[] fromSymbols, String toSymbol, String exchange) {
        String url = "https://min-api.cryptocompare.com/data/pricemultifull?"
                + "&fsyms=" + StringHelper.join(",", fromSymbols)
                + "&tsyms=" + toSymbol
                + "&e=" + exchange;
        Log.d("URL", url);
        JSONObject response = new BlockingRequest(activity, url).perform();
        return new PricesImpl(new PricesResponseImpl(response), exchange);
    }

    private ArrayList<History> getHistoryXxx(String kind, String fromSymbol, String toSymbol, int limit, int aggregate, String exchange) {
        String url = "https://min-api.cryptocompare.com/data/histo" + kind +
                "?fsym=" + fromSymbol + "&tsym=" + toSymbol + "&e=" + exchange +
                "&limit=" + limit + "&aggregate=" + aggregate;
        Log.d("URL", url);

        JSONObject response = new BlockingRequest(activity, url).perform();
        HistoryResponse historyResponse = new HistoryResponseImpl(response, fromSymbol, toSymbol);

        JSONArray histories = historyResponse.getData();
        if (histories == null) {
            Log.d("getHistoryXxx", "empty, " + kind);
            return null;
        }

        ArrayList<History> result = new ArrayList<>();

        try {
            for (int i = 0; i < histories.length(); i++) {
                result.add(new HistoryImpl(histories.getJSONObject(i), fromSymbol, toSymbol));
            }
        } catch (JSONException e) {
            Log.e("getHistoryMinute", e.getMessage());
            result = null;
        }

        return result;
    }

    @Override
    public ArrayList<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit, int aggregate, String exchange) {
        ArrayList<History> records = getHistoryXxx("minute", fromSymbol, toSymbol, limit, 1, exchange);
        if (aggregate == 1) {
            return records;
        } else {
            return sampling(records, aggregate);
        }
    }

    @Override
    public ArrayList<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit) {
        return getHistoryMinute(fromSymbol, toSymbol, limit, 1, "cccagg");
    }

    @Override
    public ArrayList<History> getHistoryHour(String fromSymbol, String toSymbol, int limit, int aggregate) {
        ArrayList<History> records = getHistoryXxx("hour", fromSymbol, toSymbol, limit, 1, "cccagg");
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
        ArrayList<History> records = getHistoryXxx("day", fromSymbol, toSymbol, limit, 1, "cccagg");
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

    @Override
    public TopPairs getTopPairs(String fromSymbol) {
        String url = "https://min-api.cryptocompare.com/data/top/pairs?fsym=" + fromSymbol + "&limit=100";
        Log.d("URL", url);

        JSONObject response = new BlockingRequest(activity, url).perform();
        TopPairsResponse topPairsResponse = new TopPairsResponseImpl(response, fromSymbol);
        return new TopPairsImpl(topPairsResponse);
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

    @Override
    public ArrayList<PriceMultiFullCoin> getBtcToplist(final CoinList coinList) {
//        StringBuilder builder = new StringBuilder();
//        ArrayList<String> symbols = coinList.getAllSymbols(20);
//
//        for (int i = 0; i < symbols.size(); i++) {
//            builder.append(symbols.get(i)).append(',');
//        }
//
//        String url = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=BTC"
//                + "&tsyms=" + builder.substring(0, builder.length() - 1);
//
//        Log.d("URL", url);
//        new NonBlockingRequest(activity, url).perform(new Request.Listener() {
//            @Override
//            public void finished(JSONObject response) {
//                ArrayList<PriceMultiFullCoin> coins = new ToplistImpl(response).getCoins();
//                for (PriceMultiFullCoin coin : coins) {
//                    Log.d("COINS", coin.getFromSymbol() + ", " + coin.getToSymbol());
//                }
//            }
//        });
//
//
        return null;
    }
}
