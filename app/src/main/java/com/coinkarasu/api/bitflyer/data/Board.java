package com.coinkarasu.api.bitflyer.data;

import com.coinkarasu.utils.CKLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Board {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "Board";

    private List<Data> asks;
    private double midPrice;
    private List<Data> bids;

    public Board(JSONObject response) {
        if (response == null) {
            return;
        }

        asks = new ArrayList<>();
        bids = new ArrayList<>();

        try {
            midPrice = response.getDouble("mid_price");

            JSONArray asksArray = response.getJSONArray("asks");
            for (int i = 0; i < asksArray.length(); i++) {
                JSONObject askObj = asksArray.getJSONObject(i);
                Data data = new Data(askObj.getDouble("price"), askObj.getDouble("size"));
                asks.add(data);
            }

            JSONArray bidsArray = response.getJSONArray("bids");
            for (int i = 0; i < bidsArray.length(); i++) {
                JSONObject bidObj = bidsArray.getJSONObject(i);
                Data data = new Data(bidObj.getDouble("price"), bidObj.getDouble("size"));
                bids.add(data);
            }

            Comparator<Data> comparator = new Comparator<Data>() {
                public int compare(Data d1, Data d2) {
                    return d1.price > d2.price ? -1 : 1; // DESC
                }
            };

            Collections.sort(bids, comparator);
            Collections.sort(asks, comparator);

        } catch (JSONException e) {
            CKLog.e(TAG, response.toString(), e);
        }

    }

    public double getOver(double upperPriceDiff) {
        double upperPrice = midPrice + upperPriceDiff;
        double over = 0.0;

        for (Data ask : asks) {
            if (ask.price >= upperPrice) {
                over += ask.size;
            }
        }

        return over;
    }

    public double getOver(int askRows) {
        int index = tailIndex(askRows, asks.size());
        double over = 0.0;

        for(int i = 0; i < index; i++) {
            over += asks.get(i).size;
        }

        return over;
    }

    public double getUnder(double lowerPriceDiff) {
        double lowerPrice = midPrice - lowerPriceDiff;
        double under = 0.0;

        for (Data bid : bids) {
            if (bid.price <= lowerPrice) {
                under += bid.size;
            }
        }

        return under;
    }

    public double getUnder(int bidRows) {
        int index = headIndex(bidRows, bids.size());
        double under = 0.0;

        for(int i = index; i < bids.size(); i++) {
            under += bids.get(i).size;
        }

        return under;
    }

    public List<Row> getRows(double lowerPriceDiff, double upperPriceDiff) {
        ArrayList<Row> rows = new ArrayList<>();

        double upperPrice = midPrice + upperPriceDiff;
        double lowerPrice = midPrice - lowerPriceDiff;

        for (Data ask : asks) {
            if (ask.price < upperPrice) {
                rows.add(new Row(ask.size, ask.price, -1));
            }
        }

        rows.add(new Row(-1, midPrice, -1));

        for (Data bid : bids) {
            if (bid.price > lowerPrice) {
                rows.add(new Row(-1, bid.price, bid.size));
            }
        }

        return rows;
    }

    private int tailIndex(int rows, int size) {
        return Math.max(size - rows, 0);
    }

    private int headIndex(int rows, int size) {
        return Math.min(rows, size - 1);
    }

    public List<Row> getRows(int askRows, int bidRows) {
        ArrayList<Row> rows = new ArrayList<>();

        List<Data> tailAsks = asks.subList(tailIndex(askRows, asks.size()), asks.size());
        List<Data> headBids = bids.subList(0, headIndex(bidRows, bids.size()));

        for (Data ask : tailAsks) {
            rows.add(new Row(ask.size, ask.price, -1));
        }

        rows.add(new Row(-1, midPrice, -1));

        for (Data bid : headBids) {
            rows.add(new Row(-1, bid.price, bid.size));
        }

        return rows;
    }

    public double getMidPrice() {
        return midPrice;
    }

    public List<Data> getAsks() {
        return asks;
    }

    public List<Data> getBids() {
        return bids;
    }

    private static class Data {
        double price;
        double size;

        Data(double price, double size) {
            this.price = price;
            this.size = size;
        }
    }

    public static class Row {
        public double askSize;
        public double price;
        public Double bidSize;

        Row(double askSize, double price, double bidSize) {
            this.askSize = askSize;
            this.price = price;
            this.bidSize = bidSize;
        }
    }
}
