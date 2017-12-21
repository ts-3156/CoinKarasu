package com.example.coinkarasu.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.coinkarasu.activities.MainFragment;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.cryptocompare.data.CoinList;
import com.example.coinkarasu.cryptocompare.data.CoinListImpl;
import com.example.coinkarasu.cryptocompare.request.BlockingRequest;
import com.example.coinkarasu.database.AppDatabase;
import com.example.coinkarasu.database.CoinListCoin;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;

public class UpdateCoinListIntentService extends IntentService {

    private static final String TAG = UpdateCoinListIntentService.class.getSimpleName();

    public UpdateCoinListIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long start = System.currentTimeMillis();
        String url = "https://www.cryptocompare.com/api/data/coinlist/";
        JSONObject response = new BlockingRequest(this, url).perform();
        CoinList coinList = CoinListImpl.buildByResponse(response);

        AppDatabase db = AppDatabase.getAppDatabase(this);
        db.coinListCoinDao().deleteAll();

        LinkedHashSet<String> uniqueSymbols = new LinkedHashSet<>();

        for (MainFragment.NavigationKind kind : MainFragment.NavigationKind.values()) {
            if (kind.symbolsResId == -1) {
                continue;
            }

            String[] array = getResources().getStringArray(kind.symbolsResId);
            Collections.addAll(uniqueSymbols, array);
        }

        ArrayList<Coin> coins = new ArrayList<>();
        ArrayList<CoinListCoin> coinListCoins = new ArrayList<>();

        for (String symbol : uniqueSymbols) {
            coins.add(coinList.getCoinBySymbol(symbol));
        }

        for (Coin coin : coins) {
            coinListCoins.add(new CoinListCoin(coin));
        }
        db.coinListCoinDao().insertCoins(coinListCoins);

        removeUnusedSymbolsFromCoinList(coinList, uniqueSymbols);
        coinList.saveToCache(this);

        Log.e("onHandleIntent", "CoinList updated, db " + db.coinListCoinDao().size() + " records, CoinList " +
                + coinList.getAllSymbols().size() + " coins " + (System.currentTimeMillis() - start) + " ms");
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