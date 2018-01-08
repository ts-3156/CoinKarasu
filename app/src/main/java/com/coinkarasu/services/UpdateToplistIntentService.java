package com.coinkarasu.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.api.cryptocompare.Client;
import com.coinkarasu.api.cryptocompare.ClientFactory;
import com.coinkarasu.api.cryptocompare.data.Prices;
import com.coinkarasu.coins.PriceMultiFullCoin;
import com.coinkarasu.data.Toplist;
import com.coinkarasu.utils.DiskCacheHelper;
import com.coinkarasu.utils.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class UpdateToplistIntentService extends IntentService {

    private static final boolean DEBUG = true;

    private static final long ONE_DAY = 24 * 60 * 60 * 1000;
    private static final String TAG = UpdateToplistIntentService.class.getSimpleName();

    public UpdateToplistIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long start = System.currentTimeMillis();
        Log logger = new Log(getApplicationContext());
        NavigationKind kind = NavigationKind.valueOf(intent.getAction());
        String symbol = kind.getToSymbol();
        String logFile = logFile(symbol);

        if (DiskCacheHelper.exists(this, logFile) && !DiskCacheHelper.isExpired(this, logFile, ONE_DAY)) {
            if (DEBUG) logger.d(TAG, kind.name() + " is recently executed.");
            return;
        }
        DiskCacheHelper.touch(this, logFile);

        String[] symbols = getResources().getStringArray(kind.symbolsResId);
        ArrayList<PriceMultiFullCoin> coins = new ArrayList<>(symbols.length);
        Client client = ClientFactory.getInstance(this);

        for (int i = 0; i < symbols.length; i += 10) {
            int index = i + 9;
            if (index >= symbols.length) {
                index = symbols.length - 1;
            }

            String[] target = Arrays.copyOfRange(symbols, i, index + 1);
            Prices prices = client.getPrices(target, symbol, "cccagg");
            coins.addAll(prices.getCoins());

            if (index >= symbols.length) {
                break;
            }
        }

        Iterator<PriceMultiFullCoin> iterator = coins.iterator();
        while (iterator.hasNext()) {
            PriceMultiFullCoin coin = iterator.next();
            if (coin.getVolume24HourTo() == 0.0) {
                iterator.remove();
            }
        }

        Collections.sort(coins, new Comparator<PriceMultiFullCoin>() {
            public int compare(PriceMultiFullCoin c1, PriceMultiFullCoin c2) {
                return c1.getVolume24HourTo() > c2.getVolume24HourTo() ? -1 : 1;
            }
        });

        Toplist toplist = new Toplist(coins, kind);
        toplist.saveToCache(this);

        if (DEBUG) logger.d(TAG, symbol + " toplist updated, "
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