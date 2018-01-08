package com.coinkarasu.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.coinkarasu.activities.HomeTabFragment;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.activities.etc.TrendingKind;
import com.coinkarasu.api.cryptocompare.Client;
import com.coinkarasu.api.cryptocompare.ClientFactory;
import com.coinkarasu.api.cryptocompare.data.CoinList;
import com.coinkarasu.api.cryptocompare.data.CoinListImpl;
import com.coinkarasu.api.cryptocompare.data.History;
import com.coinkarasu.api.cryptocompare.data.Prices;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.coins.CoinImpl;
import com.coinkarasu.coins.PriceMultiFullCoin;
import com.coinkarasu.data.Trending;
import com.coinkarasu.utils.DiskCacheHelper;
import com.coinkarasu.utils.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

public class UpdateTrendingIntentService extends IntentService {

    private static final boolean DEBUG = true;

    private static final long ONE_DAY = 24 * 60 * 60 * 1000;
    private static final String TAG = UpdateTrendingIntentService.class.getSimpleName();

    public UpdateTrendingIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//        ArrayList<String> toSymbols = new ArrayList<>();
//        for (MainActivity.Currency currency : MainActivity.Currency.values()) {
//            toSymbols.add(currency.name());
//        }
//
//        ArrayList<String> exchanges = new ArrayList<>();
//        for (Exchange exchange : Exchange.values()) {
//
//        }

        for (TrendingKind kind : TrendingKind.values()) {
            update(kind, "JPY", "zaif");
        }
    }

    private void update(TrendingKind kind, String toSymbol, String exchange) {
        long start = System.currentTimeMillis();
        Log logger = new Log(getApplicationContext());
        String logFile = logFile(kind, toSymbol, exchange);

        if (DiskCacheHelper.exists(this, logFile) && !DiskCacheHelper.isExpired(this, logFile, ONE_DAY)) {
            if (DEBUG) logger.d(TAG, kind.name() + " is recently executed.");
            return;
        }
        DiskCacheHelper.touch(this, logFile);

        LinkedHashSet<String> uniqueSymbols = new LinkedHashSet<>();
        String[] array = getResources().getStringArray(NavigationKind.japan.symbolsResId);
        Collections.addAll(uniqueSymbols, array);

        Prices prices = ClientFactory.getInstance(this).getPrices(uniqueSymbols.toArray(new String[uniqueSymbols.size()]), toSymbol, exchange);
        List<PriceMultiFullCoin> baseCoins = prices.getCoins();
        ArrayList<Coin> coins = new ArrayList<>();
        CoinList coinList = CoinListImpl.getInstance(this);

        for (PriceMultiFullCoin coin : baseCoins) {
            List<History> records = getHistories(kind, coin.getFromSymbol(), coin.getToSymbol(), exchange);
            if (records.isEmpty()) {
                continue;
            }

            History oldest = records.get(0);
            History latest = records.get(records.size() - 1);
            if (latest.getClose() - oldest.getClose() <= 0.0) {
                continue;
            }

            Coin coinListCoin = coinList.getCoinBySymbol(coin.getFromSymbol());
            Coin newCoin = CoinImpl.buildByPMFCoin(coin, coinListCoin.getFullName(), coinListCoin.getImageUrl());
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

        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra("kind", kind.name());
        broadcastIntent.setAction(HomeTabFragment.ACTION_UPDATE_TRENDING);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

        if (DEBUG) logger.d(TAG, kind.name() + " trending updated, "
                + coins.size() + " coins " + (System.currentTimeMillis() - start) + " ms");
    }

    private List<History> getHistories(TrendingKind kind, String fromSymbol, String toSymbol, String exchange) {
        Client client = ClientFactory.getInstance(this);
        List<History> records = new ArrayList<>();

        switch (kind) {
            case one_hour:
                records = client.getHistoryMinute(fromSymbol, toSymbol, 60, 1, exchange);
                break;
            case six_hours:
                records = client.getHistoryMinute(fromSymbol, toSymbol, 60 * 6, 1, exchange);
                break;
            case twelve_hours:
                records = client.getHistoryHour(fromSymbol, toSymbol, 12, 1, exchange);
                break;
            case twenty_four_hours:
                records = client.getHistoryHour(fromSymbol, toSymbol, 24, 1, exchange);
                break;
            case three_days:
                records = client.getHistoryDay(fromSymbol, toSymbol, 3, 1, exchange);
                break;
        }

        return records;
    }

    private String logFile(TrendingKind kind, String toSymbol, String exchange) {
        return UpdateTrendingIntentService.class.getSimpleName() + "-" + kind.name() + "-" + toSymbol + "-" + exchange + ".log";
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, UpdateTrendingIntentService.class);
        context.startService(intent);
    }
}