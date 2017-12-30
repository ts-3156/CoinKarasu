package com.example.coinkarasu.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.coinkarasu.activities.etc.NavigationKind;
import com.example.coinkarasu.api.cryptocompare.data.CoinList;
import com.example.coinkarasu.api.cryptocompare.data.CoinListImpl;
import com.example.coinkarasu.api.cryptocompare.request.BlockingRequest;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.database.AppDatabase;
import com.example.coinkarasu.database.CoinListCoin;
import com.example.coinkarasu.utils.CacheHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;

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
        if (CacheHelper.exists(this, LOG) && !CacheHelper.isExpired(this, LOG, THIRTY_MINUTES)) {
            if (DEBUG) Log.e("onHandleIntent", "Recently executed.");
            return;
        }
        CacheHelper.touch(this, LOG);

        long start = System.currentTimeMillis();
        String url = "https://www.cryptocompare.com/api/data/coinlist/";
        JSONObject response = new BlockingRequest(this, url).perform();
        CoinList coinList = CoinListImpl.buildByResponse(response);

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

        ArrayList<Coin> coins = new ArrayList<>(uniqueSymbols.size());
        for (String symbol : uniqueSymbols) {
            coins.add(coinList.getCoinBySymbol(symbol));
        }

        ArrayList<CoinListCoin> coinListCoins = new ArrayList<>(coins.size());
        for (Coin coin : coins) {
            coinListCoins.add(new CoinListCoin(coin));
        }
        db.coinListCoinDao().insertCoins(coinListCoins);

        removeUnusedSymbolsFromCoinList(coinList, uniqueSymbols);
        coinList.saveToCache(this);

        if (DEBUG) Log.e("onHandleIntent", "CoinList updated, db " + db.coinListCoinDao().size() + " records, CoinList " +
                +coinList.getAllSymbols().size() + " coins " + (System.currentTimeMillis() - start) + " ms");
    }

    private void removeUnusedSymbolsFromCoinList(CoinList coinList, LinkedHashSet<String> symbols) {
        ArrayList<String> unusedSymbols = new ArrayList<>();

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