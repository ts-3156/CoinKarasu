package com.coinkarasu.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.api.cryptocompare.ClientFactory;
import com.coinkarasu.api.cryptocompare.data.CoinList;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.database.AppDatabase;
import com.coinkarasu.database.CoinListCoin;
import com.coinkarasu.utils.CKDateUtils;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.IntentServiceIntervalChecker;
import com.coinkarasu.utils.PrefHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class UpdateCoinListIntentService extends IntentService {

    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "UpdateCoinListIntentService";
    private static final long THIRTY_MINUTES = TimeUnit.MINUTES.toMinutes(30);

    public UpdateCoinListIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (PrefHelper.isAirplaneModeOn(this)) {
            return;
        }

        try {
            update();
        } catch (Exception e) {
            CKLog.e(TAG, e);
        }
    }

    protected void update() {
        if (!IntentServiceIntervalChecker.shouldRun(this, TAG, THIRTY_MINUTES)) {
            return;
        }
        IntentServiceIntervalChecker.onStart(this, TAG);

        long start = CKDateUtils.now();
        CoinList coinList = ClientFactory.getInstance(this).getCoinList();

        AppDatabase db = AppDatabase.getAppDatabase(this);
        db.coinListCoinDao().deleteAll();

        Set<String> uniqueSymbols = new HashSet<>();
        for (NavigationKind kind : NavigationKind.values()) {
            if (kind.symbolsResId != -1) {
                Collections.addAll(uniqueSymbols, getResources().getStringArray(kind.symbolsResId));
            }
        }

        List<Coin> coins = new ArrayList<>(uniqueSymbols.size());
        for (String symbol : uniqueSymbols) {
            Coin c = coinList.getCoinBySymbol(symbol);
            if (c != null) {
                coins.add(coinList.getCoinBySymbol(symbol));
            }
        }

        List<CoinListCoin> insertCoins = new ArrayList<>(coins.size());
        for (Coin coin : coins) {
            insertCoins.add(new CoinListCoin(coin));
        }
        db.coinListCoinDao().insertCoins(insertCoins);

        removeUnusedSymbolsFromCoinList(coinList, uniqueSymbols);
        coinList.saveToCache(this);

        if (DEBUG) CKLog.d(TAG, "CoinList updated, db "
                + db.coinListCoinDao().size() + " records, CoinList " +
                +coinList.getAllSymbols().size() + " coins " + (CKDateUtils.now() - start) + " ms");
    }

    private static void removeUnusedSymbolsFromCoinList(CoinList coinList, Set<String> symbols) {
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
