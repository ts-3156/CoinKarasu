package com.coinkarasu.data;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.coinkarasu.activities.etc.TrendingKind;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.coins.CoinImpl;
import com.coinkarasu.utils.DiskCacheHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Trending {

    private TrendingKind kind;
    private ArrayList<Coin> coins;

    public Trending(ArrayList<Coin> coins, TrendingKind kind) {
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
            Crashlytics.logException(e);
        }

        return new Trending(coins, kind);
    }

    private static String getCacheName(TrendingKind kind) {
        return "trending_" + kind.name() + ".json";
    }

    public ArrayList<Coin> getCoins() {
        return coins;
    }

    public TrendingKind getKind() {
        return kind;
    }
}
