package com.coinkarasu.api.cryptocompare.data;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;

import com.coinkarasu.R;
import com.coinkarasu.api.cryptocompare.response.CoinListResponse;
import com.coinkarasu.api.cryptocompare.response.CoinListResponseImpl;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.database.AppDatabase;
import com.coinkarasu.database.CoinListCoin;
import com.coinkarasu.services.UpdateCoinListIntentService;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.io.CacheFileHelper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public abstract class CoinList {
    private static final boolean DEBUG = true;
    private static final String TAG = "CoinList";
    private static final String CACHE_NAME = "coin_list_response.json";

    private static CoinList instance;

    public abstract Coin getCoinBySymbol(String symbol);

    public abstract Coin getCoinByCCId(String id);

    public abstract List<Coin> collectCoins(String[] fromSymbols);

    public static List<Coin> collectCoins(Context context, String[] fromSymbols) {
        long start = System.currentTimeMillis();
        List<Coin> coins = new ArrayList<>(fromSymbols.length);
        AppDatabase db = AppDatabase.getAppDatabase(context);

        List<CoinListCoin> coinListCoins = db.coinListCoinDao().findBySymbols(fromSymbols);

        for (String symbol : fromSymbols) {
            for (CoinListCoin coinListCoin : coinListCoins) {
                if (coinListCoin.getSymbol().equals(symbol)) {
                    coins.add(Coin.buildBy(coinListCoin));
                    break;
                }
            }
        }

        if (fromSymbols.length != coins.size()) {
            if (DEBUG) CKLog.w(TAG, "collectCoins() Different size "
                    + fromSymbols.length + " symbols " + coins.size() + " coins from DB");
        }

        if (DEBUG) CKLog.d(TAG, "collectCoins() from DB " + coins.size() + " coins "
                + (System.currentTimeMillis() - start) + " ms");

        return coins;
    }

    public abstract List<String> getAllSymbols();

    public abstract List<String> getAllSymbols(int offset, int limit);

    public abstract void removeBySymbols(List<String> symbols);

    public static synchronized CoinList getInstance(Context context) {
        if (instance == null) {
            try {
                instance = restoreFromCache(context);
            } catch (Exception e) {
                CKLog.e(TAG, "getInstance1", e);
            }
        }

        if (instance == null) {
            if (DEBUG) CKLog.w(TAG, "getInstance() Failed to restore from cache");
            try {
                instance = buildBy(new JSONObject(readFromResource(context)));
                UpdateCoinListIntentService.start(context);
            } catch (Exception e) {
                CKLog.e(TAG, "getInstance2", e);
            }
        }

        return instance;
    }

    public static CoinList buildBy(JSONObject response) {
        return new CoinListImpl(new CoinListResponseImpl(response));
    }

    public void saveToCache(Context context) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            throw new RuntimeException("Should not called from the main thread");
        }
        CacheFileHelper.write(context, CACHE_NAME, toString());
    }

    private static CoinList restoreFromCache(Context context) {
        String str = CacheFileHelper.read(context, CACHE_NAME);
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return new CoinListImpl(CoinListResponse.buildBy(str));
    }

    private static String readFromResource(Context context) {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(
                    context.getResources().openRawResource(R.raw.coin_list)));

            String buf;
            while ((buf = reader.readLine()) != null) {
                builder.append(buf);
            }
        } catch (IOException e) {
            CKLog.e(TAG, e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                CKLog.e(TAG, e);
            }
        }

        return builder.toString();
    }
}
