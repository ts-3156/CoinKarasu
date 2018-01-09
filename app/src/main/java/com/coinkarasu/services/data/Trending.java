package com.coinkarasu.services.data;

import android.content.Context;

import com.coinkarasu.activities.etc.TrendingKind;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.coins.CoinImpl;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.DiskCacheHelper;
import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Trending {

    private static final boolean DEBUG = true;
    private static final String TAG = "Trending";

    private TrendingKind kind;
    private List<Coin> coins;

    public Trending(List<Coin> coins, TrendingKind kind) {
        this.coins = coins;
        this.kind = kind;
    }

    public void saveToCache(Context context) {
        JSONArray data = new JSONArray();
        for (Coin coin : coins) {
            data.put(coin.toJson());
        }

        DiskCacheHelper.write(context, getCacheName(kind), data.toString());
    }

    public static Trending restoreFromCache(Context context, TrendingKind kind) {
        String text = DiskCacheHelper.read(context, getCacheName(kind));
        if (text == null) {
            return null;
        }

        ArrayList<Coin> coins = new ArrayList<>();

        try {
            JSONArray data = new JSONArray(text);
            for (int i = 0; i < data.length(); i++) {
                JSONObject attrs = data.getJSONObject(i);
                coins.add(CoinImpl.buildByAttrs(attrs));
            }
        } catch (JSONException e) {
            if (DEBUG) CKLog.e(TAG, text, e);
        }

        return new Trending(coins, kind);
    }

    private static String getCacheName(TrendingKind kind) {
        return "trending_" + kind.name() + ".json";
    }

    public List<Coin> getCoins() {
        return coins;
    }

    public TrendingKind getKind() {
        return kind;
    }
}
