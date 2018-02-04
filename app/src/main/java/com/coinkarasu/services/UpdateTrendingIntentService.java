package com.coinkarasu.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.coinkarasu.activities.HomeTabFragment;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.activities.etc.TrendingKind;
import com.coinkarasu.api.cryptocompare.CacheMode;
import com.coinkarasu.api.cryptocompare.Client;
import com.coinkarasu.api.cryptocompare.ClientFactory;
import com.coinkarasu.api.cryptocompare.data.CoinList;
import com.coinkarasu.api.cryptocompare.data.History;
import com.coinkarasu.api.cryptocompare.data.Prices;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.coins.PriceMultiFullCoin;
import com.coinkarasu.services.data.Trending;
import com.coinkarasu.utils.CKDateUtils;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.IntentServiceIntervalChecker;
import com.coinkarasu.utils.PrefHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UpdateTrendingIntentService extends IntentService {

    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = UpdateTrendingIntentService.class.getSimpleName();

    public UpdateTrendingIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (PrefHelper.isAirplaneModeOn(this)) {
            return;
        }

        for (TrendingKind kind : TrendingKind.values()) {
            try {
                update(kind, Exchange.cccagg, "JPY", intent.getBooleanExtra("force", false));
            } catch (Exception e) {
                CKLog.e(TAG, e);
            }
        }
    }

    private void update(TrendingKind kind, Exchange exchange, String toSymbol, boolean force) {
        long start = CKDateUtils.now();
        String tag = TAG + "-" + kind.name() + "-" + toSymbol + "-" + exchange;

        if (!force && !IntentServiceIntervalChecker.shouldRun(this, tag, kind.expiration)) {
            return;
        }
        IntentServiceIntervalChecker.onStart(this, tag);
        sendBroadcast(kind, "started");

        Set<String> uniqueSymbols = new HashSet<>(); // 日本で取引できるコインとCoincheckのコインの重複のない一覧
        Collections.addAll(uniqueSymbols, getResources().getStringArray(NavigationKind.japan.symbolsResId));
        Collections.addAll(uniqueSymbols, getResources().getStringArray(NavigationKind.coincheck.symbolsResId));

        Prices prices = ClientFactory.getInstance(this)
                .getPrices(uniqueSymbols.toArray(new String[uniqueSymbols.size()]), toSymbol, exchange.name());
        List<PriceMultiFullCoin> baseCoins = prices.getCoins();
        List<Coin> coins = new ArrayList<>();
        CoinList coinList = CoinList.getInstance(this);

        for (PriceMultiFullCoin coin : baseCoins) {
            List<History> records = getHistories(kind, coin.getFromSymbol(), coin.getToSymbol(), exchange.name());
            if (records == null || records.isEmpty()) {
                continue;
            }

            History oldest = records.get(0);
            History latest = records.get(records.size() - 1);
            // if (latest.getClose() - oldest.getClose() <= 0.0) {
            //     continue;
            // }

            Coin coinListCoin = coinList.getCoinBySymbol(coin.getFromSymbol());
            Coin newCoin = Coin.buildBy(coin, coinListCoin.getFullName(), coinListCoin.getImageUrl());
            newCoin.setToSymbol(coin.getToSymbol());
            newCoin.setPrice(oldest.getClose());
            newCoin.setPrice(latest.getClose());
            newCoin.setTrend((latest.getClose() - oldest.getClose()) / oldest.getClose());
            coins.add(newCoin);
        }

        Collections.sort(coins, new Comparator<Coin>() {
            public int compare(Coin c1, Coin c2) {
                return c1.getTrend() > c2.getTrend() ? -1 : 1;
            }
        });

        Trending trending = new Trending(coins, kind);
        trending.saveToCache(this);

        sendBroadcast(kind, "finished");

        if (DEBUG) CKLog.d(TAG, kind.name() + " " + exchange.name() + " " + toSymbol + " trending updated, "
                + coins.size() + " coins " + (CKDateUtils.now() - start) + " ms");
    }

    private void sendBroadcast(TrendingKind kind, String progress) {
        Intent intent = new Intent();
        intent.putExtra("kind", kind.name());
        intent.putExtra("progress", progress);
        intent.setAction(HomeTabFragment.ACTION_UPDATE_TRENDING);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private List<History> getHistories(TrendingKind kind, String fromSymbol, String toSymbol, String exchange) {
        Client client = ClientFactory.getInstance(this);
        List<History> records = new ArrayList<>();

        switch (kind) {
            case one_hour:
                records = client.getHistoryMinute(fromSymbol, toSymbol, 60, 1, exchange, CacheMode.NORMAL);
                break;
            case six_hours:
                records = client.getHistoryMinute(fromSymbol, toSymbol, 60 * 6, 1, exchange, CacheMode.NORMAL);
                break;
            case twelve_hours:
                records = client.getHistoryHour(fromSymbol, toSymbol, 12, 1, exchange, CacheMode.NORMAL);
                break;
            case twenty_four_hours:
                records = client.getHistoryHour(fromSymbol, toSymbol, 24, 1, exchange, CacheMode.NORMAL);
                break;
            case three_days:
                records = client.getHistoryDay(fromSymbol, toSymbol, 3, 1, exchange, CacheMode.NORMAL);
                break;
        }

        return records;
    }

    public static void start(Context context, boolean force) {
        Intent intent = new Intent(context, UpdateTrendingIntentService.class);
        intent.putExtra("force", force);
        context.startService(intent);
    }
}
