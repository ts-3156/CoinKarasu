package com.coinkarasu.api.cryptocompare;

import android.content.Context;

import com.coinkarasu.api.cryptocompare.data.CoinList;
import com.coinkarasu.api.cryptocompare.data.CoinSnapshot;
import com.coinkarasu.api.cryptocompare.data.CoinSnapshotImpl;
import com.coinkarasu.api.cryptocompare.data.HistoriesCache;
import com.coinkarasu.api.cryptocompare.data.History;
import com.coinkarasu.api.cryptocompare.data.Prices;
import com.coinkarasu.api.cryptocompare.data.PricesImpl;
import com.coinkarasu.api.cryptocompare.data.TopPairs;
import com.coinkarasu.api.cryptocompare.data.TopPairsImpl;
import com.coinkarasu.api.cryptocompare.request.BlockingRequest;
import com.coinkarasu.api.cryptocompare.response.CoinSnapshotResponse;
import com.coinkarasu.api.cryptocompare.response.CoinSnapshotResponseImpl;
import com.coinkarasu.api.cryptocompare.response.HistoryResponseImpl;
import com.coinkarasu.api.cryptocompare.response.HistoryResponseImpl.HistoryKind;
import com.coinkarasu.api.cryptocompare.response.PricesResponseImpl;
import com.coinkarasu.api.cryptocompare.response.TopPairsResponse;
import com.coinkarasu.api.cryptocompare.response.TopPairsResponseImpl;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.CKStringUtils;
import com.coinkarasu.utils.volley.RequestQueueWrapper;
import com.coinkarasu.utils.volley.VolleyHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class ClientImpl implements Client {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "ClientImpl";
    private static final String DEFAULT_EXCHANGE = "cccagg";

    private Context context;
    private RequestQueueWrapper requestQueue;

    ClientImpl(Context context) {
        this.context = context;
        this.requestQueue = VolleyHelper.getInstance(context).getWrappedRequestQueue();
    }

    /**
     * 秒単位で最新のデータを使う必要があるため、この階層ではキャッシュしていない。
     */
    @Override
    public Prices getPrices(String[] fromSymbols, String toSymbol, String exchange) {
        String url = "https://min-api.cryptocompare.com/data/pricemultifull?"
                + Query.toString("fsyms", CKStringUtils.join(fromSymbols, ","), "tsyms", toSymbol, "e", exchange);
        JSONObject response = performGet(url);
        return new PricesImpl(new PricesResponseImpl(response));
    }

    /**
     * 他のAPIに比べて非常に多くのリクエストを行うため、可能な限りリクエストを減らすためにmodeフラグを持っている。
     * 他のAPIでは、キャッシュの有無や有効期限に関わらず必ず新しくリクエストする。このAPIでは、キャッシュの有効期限が切れている場合のみ新しくリクエストする。
     * <p>
     * このAPIでは、キャッシュのキーに使うパラメーターをHTTPアクセスのパラメーターと極力揃えて、
     * 可能な限り粒度の小さいキャッシングを行っている。
     */
    private List<History> getHistoryXxx(HistoryKind kind, String fromSymbol, String toSymbol, int limit, int aggregate, String exchange, int mode) {
        if (mode == CacheMode.NONE) {
            throw new UnsupportedOperationException();
        }

        List<History> histories;
        HistoriesCache cache = new HistoriesCache(context);

        if (!isFlagOn(mode, CacheMode.FORCE_IF_EXPIRED) && isFlagOn(mode, CacheMode.NORMAL | CacheMode.READ_ONLY)) {
            histories = cache.get(kind, fromSymbol, toSymbol, limit, aggregate, exchange, (mode & CacheMode.IGNORE_EXPIRES) != 0);

            if (histories != null && !histories.isEmpty()) {
                if (DEBUG) CKLog.d(TAG, "getHistoryXxx() Return cache kind=" + kind.name() + " limit=" + limit);
                return histories;
            }
        }

        if (!isFlagOn(mode, CacheMode.FORCE_IF_EXPIRED) && isFlagOn(mode, CacheMode.READ_ONLY)) {
            if (DEBUG) CKLog.d(TAG, "getHistoryXxx() Flag is READ_ONLY and return null kind=" + kind.name() + " limit=" + limit);
            return null;
        }

        if (isFlagOn(mode, CacheMode.FORCE_IF_EXPIRED)
                && cache.exists(kind, fromSymbol, toSymbol, limit, aggregate, exchange)
                && !cache.isExpired(kind, fromSymbol, toSymbol, limit, aggregate, exchange)) {
            if (DEBUG) CKLog.d(TAG, "getHistoryXxx() Flag is FORCE_IF_EXPIRED and return null kind=" + kind.name() + " limit=" + limit);
            return null;
        }

        String url = "https://min-api.cryptocompare.com/data/histo" + kind + "?"
                + Query.toString("fsym", fromSymbol, "tsym", toSymbol, "e", exchange, "limit", limit, "aggregate", aggregate);

        histories = new HistoryResponseImpl(performGet(url), fromSymbol, toSymbol).getHistories();

        if (histories != null && !histories.isEmpty()) {
            new HistoriesCache(context).put(kind, fromSymbol, toSymbol, limit, aggregate, exchange, histories);
        }

        if (DEBUG) CKLog.d(TAG, "getHistoryXxx() Return histories kind=" + kind.name() + " limit=" + limit);

        return histories;
    }

    @Override
    public List<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit, int sampling, String exchange, int mode) {
        List<History> records = getHistoryXxx(HistoryKind.minute, fromSymbol, toSymbol, limit, 1, exchange, mode);
        if (sampling == 1) {
            return records;
        } else {
            return sampling(records, sampling);
        }
    }

    @Override
    public List<History> getHistoryMinute(String fromSymbol, String toSymbol, int limit, int mode) {
        return getHistoryMinute(fromSymbol, toSymbol, limit, 1, DEFAULT_EXCHANGE, mode);
    }

    @Override
    public List<History> getHistoryHour(String fromSymbol, String toSymbol, int limit, int aggregate, int mode) {
        return getHistoryHour(fromSymbol, toSymbol, limit, aggregate, DEFAULT_EXCHANGE, mode);
    }

    @Override
    public List<History> getHistoryHour(String fromSymbol, String toSymbol, int limit, int mode) {
        return getHistoryHour(fromSymbol, toSymbol, limit, 1, mode);
    }

    @Override
    public List<History> getHistoryHour(String fromSymbol, String toSymbol, int limit, int sampling, String exchange, int mode) {
        List<History> records = getHistoryXxx(HistoryKind.hour, fromSymbol, toSymbol, limit, 1, exchange, mode);
        if (sampling == 1) {
            return records;
        } else {
            return sampling(records, sampling);
        }
    }

    @Override
    public List<History> getHistoryDay(String fromSymbol, String toSymbol, int limit, int sampling, int mode) {
        return getHistoryDay(fromSymbol, toSymbol, limit, sampling, DEFAULT_EXCHANGE, mode);
    }

    @Override
    public List<History> getHistoryDay(String fromSymbol, String toSymbol, int limit, int sampling, String exchange, int mode) {
        List<History> records = getHistoryXxx(HistoryKind.day, fromSymbol, toSymbol, limit, 1, exchange, mode);
        if (sampling == 1) {
            return records;
        } else {
            return sampling(records, sampling);
        }
    }

    @Override
    public CoinSnapshot getCoinSnapshot(String fromSymbol, String toSymbol) {
        String url = "https://www.cryptocompare.com/api/data/coinsnapshot/?"
                + Query.toString("fsym", fromSymbol, "tsym", toSymbol);

        CoinSnapshotResponse response = new CoinSnapshotResponseImpl(performGet(url), fromSymbol, toSymbol);
        return new CoinSnapshotImpl(response);
    }

    @Override
    public TopPairs getTopPairs(String fromSymbol) {
        String url = "https://min-api.cryptocompare.com/data/top/pairs?"
                + Query.toString("fsym", fromSymbol, "limit", 100);

        TopPairsResponse response;

        if (TopPairsResponseImpl.isCacheExist(context, fromSymbol)) {
            response = TopPairsResponseImpl.restoreFromCache(context, fromSymbol);
        } else {
            response = new TopPairsResponseImpl(performGet(url), fromSymbol);
            response.saveToCache(context);
        }

        return new TopPairsImpl(response);
    }

    @Override
    public CoinList getCoinList() {
        String url = "https://www.cryptocompare.com/api/data/coinlist/";
        return CoinList.buildBy(performGet(url));
    }

    private List<History> sampling(List<History> records, int aggregate) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        List<History> samples = new ArrayList<>();
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

    private static boolean isFlagOn(int value, int flag) {
        return (value & flag) != 0;
    }

    private static class Query {
        private static final String AMP = "&";
        private static final String EQL = "=";

        public static String toString(Object... params) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < params.length; i += 2) {
                if (builder.length() != 0) {
                    builder.append(AMP);
                }
                builder.append(params[i]);
                builder.append(EQL);
                builder.append(params[i + 1]);
            }
            return builder.toString();
        }
    }
}
