package com.coinkarasu.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.api.cryptocompare.data.CoinList;
import com.coinkarasu.api.cryptocompare.request.BlockingRequest;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.database.AppDatabase;
import com.coinkarasu.database.CoinListCoin;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.io.CacheFileHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public class UpdateCoinListIntentService extends IntentService {

    private static final boolean DEBUG = true;

    private static final String LOG = UpdateCoinListIntentService.class.getSimpleName() + ".log";
    private static final long THIRTY_MINUTES = 30 * 60 * 1000;
    private static final String TAG = UpdateCoinListIntentService.class.getSimpleName();

    public UpdateCoinListIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            update();
        } catch (Exception e) {
            CKLog.e(TAG, e);
        }
    }

    protected void update() {
        if (CacheFileHelper.exists(this, LOG) && !CacheFileHelper.isExpired(this, LOG, THIRTY_MINUTES)) {
            if (DEBUG) CKLog.d(TAG, "Recently executed.");
            return;
        }
        CacheFileHelper.touch(this, LOG);

        long start = System.currentTimeMillis();
        String url = "https://www.cryptocompare.com/api/data/coinlist/";
        JSONObject response = new BlockingRequest(this, url).perform();
        CoinList coinList = CoinList.buildBy(response);

        AppDatabase db = AppDatabase.getAppDatabase(this);
        db.coinListCoinDao().deleteAll();

        LinkedHashSet<String> uniqueSymbols = new LinkedHashSet<>();
        for (NavigationKind kind : NavigationKind.values()) {
            if (kind.symbolsResId == -1) {
                continue;
            }

            String[] array = getResources().getStringArray(kind.symbolsResId);
            Collections.addAll(uniqueSymbols, array);
        }

        List<Coin> coins = new ArrayList<>(uniqueSymbols.size());
        for (String symbol : uniqueSymbols) {
            coins.add(coinList.getCoinBySymbol(symbol));
        }

        List<CoinListCoin> coinListCoins = new ArrayList<>(coins.size());
        for (Coin coin : coins) {
            coinListCoins.add(new CoinListCoin(coin));
        }
        db.coinListCoinDao().insertCoins(coinListCoins);

        removeUnusedSymbolsFromCoinList(coinList, uniqueSymbols);
        coinList.saveToCache(this);

        if (DEBUG) CKLog.d(TAG, "CoinList updated, db "
                + db.coinListCoinDao().size() + " records, CoinList " +
                +coinList.getAllSymbols().size() + " coins " + (System.currentTimeMillis() - start) + " ms");
    }

    private void removeUnusedSymbolsFromCoinList(CoinList coinList, LinkedHashSet<String> symbols) {
        List<String> unusedSymbols = new ArrayList<>();

        for (String symbol : coinList.getAllSymbols()) {
            if (!symbols.contains(symbol)) {
                unusedSymbols.add(symbol);
            }
        }

        coinList.removeBySymbols(unusedSymbols);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, UpdateCoinListIntentService.class);
        context.startService(intent);
    }
}