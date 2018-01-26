package com.coinkarasu.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.api.cryptocompare.Client;
import com.coinkarasu.api.cryptocompare.ClientFactory;
import com.coinkarasu.api.cryptocompare.data.Prices;
import com.coinkarasu.coins.PriceMultiFullCoin;
import com.coinkarasu.services.data.Toplist;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.CKStringUtils;
import com.coinkarasu.utils.io.CacheFileHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class UpdateToplistIntentService extends IntentService {

    private static final boolean DEBUG = true;
    private static final String TAG = "UpdateToplistIntentService";
    private static final long ONE_HOUR = TimeUnit.MINUTES.toMillis(60);

    public UpdateToplistIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            update(intent);
        } catch (Exception e) {
            CKLog.e(TAG, e);
        }
    }

    protected void update(Intent intent) {
        long start = System.currentTimeMillis();
        NavigationKind kind = NavigationKind.valueOf(intent.getAction());
        String toSymbol = kind.getToSymbol();
        String logFile = logFile(toSymbol);

        if (CacheFileHelper.exists(this, logFile) && !CacheFileHelper.isExpired(this, logFile, ONE_HOUR)) {
            if (DEBUG) CKLog.d(TAG, kind.name() + " is recently executed.");
            return;
        }
        CacheFileHelper.touch(this, logFile);

        Set<String> uniqueSymbols = new HashSet<>();
        for (NavigationKind k : NavigationKind.values()) {
            if (k.isToplist()) {
                Collections.addAll(uniqueSymbols, getResources().getStringArray(k.symbolsResId));
            }
        }
        List<String> symbols = new ArrayList<>(uniqueSymbols);

        List<PriceMultiFullCoin> coins = new ArrayList<>(symbols.size());
        Client client = ClientFactory.getInstance(this);

        for (int i = 0; i < symbols.size(); i += 20) {
            int index = i + 19;
            if (index >= symbols.size()) {
                index = symbols.size() - 1;
            }

            String[] fromSymbols = symbols.subList(i, index + 1).toArray(new String[0]);
            Prices prices = client.getPrices(fromSymbols, toSymbol, "cccagg");

            if (prices == null || prices.getCoins() == null || prices.getCoins().isEmpty()) {
                if (DEBUG) CKLog.w(TAG, "Stop updating. prices is null");
                return;
            }

            coins.addAll(prices.getCoins());

            if (index >= symbols.size()) {
                break;
            }
        }

        List<String> removedSymbols = new ArrayList<>();
        Iterator<PriceMultiFullCoin> iterator = coins.iterator();
        while (iterator.hasNext()) {
            PriceMultiFullCoin coin = iterator.next();
            if (coin.getVolume24HourTo() == 0.0) {
                iterator.remove();
                removedSymbols.add(coin.getFromSymbol());
            }
        }
        if (DEBUG) CKLog.d(TAG, "removed symbols in " + kind.name() + " " + CKStringUtils.join(" ", removedSymbols));

        Collections.sort(coins, new Comparator<PriceMultiFullCoin>() {
            public int compare(PriceMultiFullCoin c1, PriceMultiFullCoin c2) {
                return c1.getVolume24HourTo() > c2.getVolume24HourTo() ? -1 : 1;
            }
        });

        if (kind != NavigationKind.btc_toplist && coins.size() > 100) {
            coins = coins.subList(0, 100);
        }

        Toplist toplist = new Toplist(coins, kind);
        toplist.saveToCache(this);

        if (DEBUG) CKLog.d(TAG, toSymbol + " toplist updated, "
                + coins.size() + " coins " + (System.currentTimeMillis() - start) + " ms");
    }

    private String logFile(String symbol) {
        return UpdateToplistIntentService.class.getSimpleName() + "-" + symbol + ".log";
    }

    public static void start(Context context, NavigationKind kind) {
        Intent intent = new Intent(context, UpdateToplistIntentService.class);
        intent.setAction(kind.name());
        context.startService(intent);
    }
}