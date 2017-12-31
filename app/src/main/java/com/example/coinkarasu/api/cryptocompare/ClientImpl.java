package com.example.coinkarasu.api.cryptocompare;

import android.content.Context;
import android.util.Log;

import com.example.coinkarasu.coins.PriceMultiFullCoin;
import com.example.coinkarasu.api.cryptocompare.data.CoinList;
import com.example.coinkarasu.api.cryptocompare.data.CoinSnapshot;
import com.example.coinkarasu.api.cryptocompare.data.CoinSnapshotImpl;
import com.example.coinkarasu.api.cryptocompare.data.History;
import com.example.coinkarasu.api.cryptocompare.data.Price;
import com.example.coinkarasu.api.cryptocompare.data.PriceImpl;
import com.example.coinkarasu.api.cryptocompare.data.Prices;
import com.example.coinkarasu.api.cryptocompare.data.PricesImpl;
import com.example.coinkarasu.api.cryptocompare.data.TopPair;
import com.example.coinkarasu.api.cryptocompare.data.TopPairsImpl;
import com.example.coinkarasu.api.cryptocompare.request.BlockingRequest;
import com.example.coinkarasu.api.cryptocompare.response.CoinSnapshotResponse;
import com.example.coinkarasu.api.cryptocompare.response.CoinSnapshotResponseImpl;
import com.example.coinkarasu.api.cryptocompare.response.HistoryResponse;
import com.example.coinkarasu.api.cryptocompare.response.HistoryResponseImpl;
import com.example.coinkarasu.api.cryptocompare.response.PricesResponseImpl;
import com.example.coinkarasu.api.cryptocompare.response.TopPairsResponse;
import com.example.coinkarasu.api.cryptocompare.response.TopPairsResponseImpl;
import com.example.coinkarasu.utils.StringHelper;

import org.json.JSONObject;

import java.util.ArrayList;

class ClientImpl implements Client {

    private static final String DEFAULT_EXCHANGE = "cccagg";

    private Context context;

    ClientImpl(Context context) {
        this.context = context;
    }

    @Override
    public Price getPrice(String fromSymbol, String toSymbol, String exchange) {
        String url = "https://min-api.cryptocompare.com/data/pricemultifull?"
                + "&fsyms=" + fromSymbol
                + "&tsyms=" + toSymbol
                + "&e=" + exchange;

        JSONObject response = new BlockingRequest(context, url).perform();
        return new PriceImpl(new PricesResponseImpl(response, new String[]{fromSymbol}, toSymbol, exchange));
    }

    @Override
    public Prices getPrices(String[] fromSymbols, String toSymbol, String exchange) {
        String url = "https://min-api.cryptocompare.com/data/pricemultifull?"
                + "&fsyms=" + StringHelper.join(",", fromSymbols)
                + "&tsyms=" + toSymbol
                + "&e=" + exchange;
        JSONObject response = new BlockingRequest(context, url).perform();
        return new PricesImpl(new PricesResponseImpl(response, fromSymbols, toSymbol, exchange));
    }

    private ArrayList<History> getHistoryXxx(HistoryResponseImpl.Kind kind, String fromSymbol, String toSymbol, int limit, int aggregate, String exchange) {
        String url = "https://min-api.cryptocompare.com/data/histo" + kind +
                "?fsym=" + fromSymbol + "&tsym=" + toSymbol + "&e=" + exchange +
                "&limit=" + limit + "&aggregate=" + aggregate;

        HistoryResponse historyResponse;

        if (HistoryResponseImpl.isCacheExist(context, fromSymbol, toSymbol, kind, limit, exchange)) {
            historyResponse = HistoryResponseImpl.restoreFromCache(context, fromSymbol, toSymbol, kind, limit, exchange);
        } else {
            JSONObject response = new BlockingRequest(context, url).perform();
            historyResponse = new HistoryResponseImpl(response, fromSymbol, toSymbol, kind, limit, exchange);
            historyResponse.saveToCache(context);
        }

        return historyResponse.getHistories();
    }

    @Override
    public ArrayList<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit, int aggregate, String exchange) {
        ArrayList<History> records = getHistoryXxx(HistoryResponseImpl.Kind.minute, fromSymbol, toSymbol, limit, 1, exchange);
        if (aggregate == 1) {
            return records;
        } else {
            return sampling(records, aggregate);
        }
    }

    @Override
    public ArrayList<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit) {
        return getHistoryMinute(fromSymbol, toSymbol, limit, 1, DEFAULT_EXCHANGE);
    }

    @Override
    public ArrayList<History> getHistoryHour(String fromSymbol, String toSymbol, int limit, int aggregate) {
        return getHistoryHour(fromSymbol, toSymbol, limit, aggregate, DEFAULT_EXCHANGE);
    }

    @Override
    public ArrayList<History> getHistoryHour(String fromSymbol, String toSymbol, int limit) {
        return getHistoryHour(fromSymbol, toSymbol, limit, 1);
    }

    @Override
    public ArrayList<History> getHistoryHour(String fromSymbol, String toSymbol, int limit, int aggregate, String exchange) {
        ArrayList<History> records = getHistoryXxx(HistoryResponseImpl.Kind.hour, fromSymbol, toSymbol, limit, 1, exchange);
        if (aggregate == 1) {
            return records;
        } else {
            return sampling(records, aggregate);
        }
    }

    @Override
    public ArrayList<History> getHistoryDay(String fromSymbol, String toSymbol, int limit) {
        return getHistoryDay(fromSymbol, toSymbol, limit, 1);
    }

    @Override
    public ArrayList<History> getHistoryDay(String fromSymbol, String toSymbol, int limit, int aggregate) {
        return getHistoryDay(fromSymbol, toSymbol, limit, aggregate, DEFAULT_EXCHANGE);
    }

    @Override
    public ArrayList<History> getHistoryDay(String fromSymbol, String toSymbol, int limit, int aggregate, String exchange) {
        ArrayList<History> records = getHistoryXxx(HistoryResponseImpl.Kind.day, fromSymbol, toSymbol, limit, 1, exchange);
        if (aggregate == 1) {
            return records;
        } else {
            return sampling(records, aggregate);
        }
    }

    @Override
    public CoinSnapshot getCoinSnapshot(String fromSymbol, String toSymbol) {
        String url = "https://www.cryptocompare.com/api/data/coinsnapshot/?fsym=" + fromSymbol + "&tsym=" + toSymbol;

        JSONObject response = new BlockingRequest(context, url).perform();
        CoinSnapshotResponse snapshotResponse = new CoinSnapshotResponseImpl(response, fromSymbol, toSymbol);
        return new CoinSnapshotImpl(snapshotResponse);
    }

    @Override
    public ArrayList<TopPair> getTopPairs(String fromSymbol) {
        String url = "https://min-api.cryptocompare.com/data/top/pairs?fsym=" + fromSymbol + "&limit=100";

        TopPairsResponse topPairsResponse;

        if (TopPairsResponseImpl.isCacheExist(context, fromSymbol)) {
            topPairsResponse = TopPairsResponseImpl.restoreFromCache(context, fromSymbol);
        } else {
            JSONObject response = new BlockingRequest(context, url).perform();
            topPairsResponse = new TopPairsResponseImpl(response, fromSymbol);
            topPairsResponse.saveToCache(context);
        }

        return new TopPairsImpl(topPairsResponse).getTopPairs();
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
