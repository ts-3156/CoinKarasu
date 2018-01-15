package com.coinkarasu.api.cryptocompare;

import android.content.Context;

import com.coinkarasu.api.cryptocompare.data.CoinSnapshot;
import com.coinkarasu.api.cryptocompare.data.CoinSnapshotImpl;
import com.coinkarasu.api.cryptocompare.data.History;
import com.coinkarasu.api.cryptocompare.data.Price;
import com.coinkarasu.api.cryptocompare.data.PriceImpl;
import com.coinkarasu.api.cryptocompare.data.Prices;
import com.coinkarasu.api.cryptocompare.data.PricesImpl;
import com.coinkarasu.api.cryptocompare.data.TopPair;
import com.coinkarasu.api.cryptocompare.data.TopPairsImpl;
import com.coinkarasu.api.cryptocompare.request.BlockingRequest;
import com.coinkarasu.api.cryptocompare.response.CoinSnapshotResponse;
import com.coinkarasu.api.cryptocompare.response.CoinSnapshotResponseImpl;
import com.coinkarasu.api.cryptocompare.response.HistoryResponse;
import com.coinkarasu.api.cryptocompare.response.HistoryResponseImpl;
import com.coinkarasu.api.cryptocompare.response.PricesResponseImpl;
import com.coinkarasu.api.cryptocompare.response.TopPairsResponse;
import com.coinkarasu.api.cryptocompare.response.TopPairsResponseImpl;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.StringHelper;
import com.coinkarasu.utils.volley.RequestQueueWrapper;
import com.coinkarasu.utils.volley.VolleyHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class ClientImpl implements Client {

    private static final boolean DEBUG = true;
    private static final String TAG = "ClientImpl";
    private static final String DEFAULT_EXCHANGE = "cccagg";

    private Context context;
    private RequestQueueWrapper requestQueue;

    ClientImpl(Context context) {
        this.context = context;
        this.requestQueue = VolleyHelper.getInstance(context).getWrappedRequestQueue();
    }

    @Override
    public Price getPrice(String fromSymbol, String toSymbol, String exchange) {
        String url = "https://min-api.cryptocompare.com/data/pricemultifull?"
                + "&fsyms=" + fromSymbol
                + "&tsyms=" + toSymbol
                + "&e=" + exchange;

        JSONObject response = performGet(url);
        return new PriceImpl(new PricesResponseImpl(response, new String[]{fromSymbol}, toSymbol, exchange));
    }

    @Override
    public Prices getPrices(String[] fromSymbols, String toSymbol, String exchange) {
        String url = "https://min-api.cryptocompare.com/data/pricemultifull?"
                + "&fsyms=" + StringHelper.join(",", fromSymbols)
                + "&tsyms=" + toSymbol
                + "&e=" + exchange;
        JSONObject response = performGet(url);
        return new PricesImpl(new PricesResponseImpl(response, fromSymbols, toSymbol, exchange));
    }

    private List<History> getHistoryXxx(HistoryResponseImpl.Kind kind, String fromSymbol, String toSymbol, int limit, int aggregate, String exchange) {
        String url = "https://min-api.cryptocompare.com/data/histo" + kind +
                "?fsym=" + fromSymbol + "&tsym=" + toSymbol + "&e=" + exchange +
                "&limit=" + limit + "&aggregate=" + aggregate;

        HistoryResponse historyResponse;

        if (HistoryResponseImpl.isCacheExist(context, fromSymbol, toSymbol, kind, limit, exchange)) {
            historyResponse = HistoryResponseImpl.restoreFromCache(context, fromSymbol, toSymbol, kind, limit, exchange);
        } else {
            JSONObject response = performGet(url);
            historyResponse = new HistoryResponseImpl(response, fromSymbol, toSymbol, kind, limit, exchange);
            historyResponse.saveToCache(context);
        }

        return historyResponse.getHistories();
    }

    @Override
    public List<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit, int aggregate, String exchange) {
        List<History> records = getHistoryXxx(HistoryResponseImpl.Kind.minute, fromSymbol, toSymbol, limit, 1, exchange);
        if (aggregate == 1) {
            return records;
        } else {
            return sampling(records, aggregate);
        }
    }

    @Override
    public List<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit) {
        return getHistoryMinute(fromSymbol, toSymbol, limit, 1, DEFAULT_EXCHANGE);
    }

    @Override
    public List<History> getHistoryHour(String fromSymbol, String toSymbol, int limit, int aggregate) {
        return getHistoryHour(fromSymbol, toSymbol, limit, aggregate, DEFAULT_EXCHANGE);
    }

    @Override
    public List<History> getHistoryHour(String fromSymbol, String toSymbol, int limit) {
        return getHistoryHour(fromSymbol, toSymbol, limit, 1);
    }

    @Override
    public List<History> getHistoryHour(String fromSymbol, String toSymbol, int limit, int aggregate, String exchange) {
        List<History> records = getHistoryXxx(HistoryResponseImpl.Kind.hour, fromSymbol, toSymbol, limit, 1, exchange);
        if (aggregate == 1) {
            return records;
        } else {
            return sampling(records, aggregate);
        }
    }

    @Override
    public List<History> getHistoryDay(String fromSymbol, String toSymbol, int limit) {
        return getHistoryDay(fromSymbol, toSymbol, limit, 1);
    }

    @Override
    public List<History> getHistoryDay(String fromSymbol, String toSymbol, int limit, int aggregate) {
        return getHistoryDay(fromSymbol, toSymbol, limit, aggregate, DEFAULT_EXCHANGE);
    }

    @Override
    public List<History> getHistoryDay(String fromSymbol, String toSymbol, int limit, int aggregate, String exchange) {
        List<History> records = getHistoryXxx(HistoryResponseImpl.Kind.day, fromSymbol, toSymbol, limit, 1, exchange);
        if (aggregate == 1) {
            return records;
        } else {
            return sampling(records, aggregate);
        }
    }

    @Override
    public CoinSnapshot getCoinSnapshot(String fromSymbol, String toSymbol) {
        String url = "https://www.cryptocompare.com/api/data/coinsnapshot/?fsym=" + fromSymbol + "&tsym=" + toSymbol;

        JSONObject response = performGet(url);
        CoinSnapshotResponse snapshotResponse = new CoinSnapshotResponseImpl(response, fromSymbol, toSymbol);
        return new CoinSnapshotImpl(snapshotResponse);
    }

    @Override
    public List<TopPair> getTopPairs(String fromSymbol) {
        String url = "https://min-api.cryptocompare.com/data/top/pairs?fsym=" + fromSymbol + "&limit=100";

        TopPairsResponse topPairsResponse;

        if (TopPairsResponseImpl.isCacheExist(context, fromSymbol)) {
            topPairsResponse = TopPairsResponseImpl.restoreFromCache(context, fromSymbol);
        } else {
            JSONObject response = performGet(url);
            topPairsResponse = new TopPairsResponseImpl(response, fromSymbol);
            topPairsResponse.saveToCache(context);
        }

        return new TopPairsImpl(topPairsResponse).getTopPairs();
    }

    private List<History> sampling(List<History> records, int aggregate) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        ArrayList<History> samples = new ArrayList<>();
        int size = records.size();

        for (int i = 0; i < size; i++) {
            if ((i < 3 || i > (size - 1 - 3)) || i % aggregate == 0) {
                samples.add(records.get(i));
            }
        }

        if (DEBUG) CKLog.d(TAG, "aggregate " + aggregate + ", records "
                + records.size() + ", samples " + samples.size());

        return samples;
    }

    private JSONObject performGet(String url) {
        return new BlockingRequest(requestQueue, url).perform();
    }
}
