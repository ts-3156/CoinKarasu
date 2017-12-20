package com.example.coinkarasu.data;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.example.coinkarasu.activities.HomeTabFragment.Kind;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.coins.CoinImpl;
import com.example.coinkarasu.utils.CacheHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public abstract class TrendingBase {

    private Kind kind;
    private ArrayList<Coin> coins;

    TrendingBase(ArrayList<Coin> coins, Kind kind) {
        this.coins = coins;
        this.kind = kind;
    }

    public void saveToCache(Context context) {
        JSONArray data = new JSONArray();
        for (Coin coin : coins) {
            data.put(coin.toJson());
        }

        CacheHelper.write(context, getCacheName(kind), data.toString());
    }

    public static Trending restoreFromCache(Context context, Kind kind) {
        String text = CacheHelper.read(context, getCacheName(kind));
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

    private static String getCacheName(Kind kind) {
        return "trending_" + kind.name() + ".json";
    }

    public ArrayList<Coin> getCoins() {
        return coins;
    }

    public Kind getKind() {
        return kind;
    }
}
