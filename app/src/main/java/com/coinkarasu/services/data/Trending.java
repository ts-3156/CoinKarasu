package com.coinkarasu.services.data;

import android.content.Context;
import android.text.TextUtils;

import com.coinkarasu.activities.etc.TrendingKind;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.io.CacheFileHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Trending {

    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "Trending";

    private TrendingKind kind;
    private List<Coin> coins;
    private Date updated;

    public Trending(List<Coin> coins, TrendingKind kind) {
        this(coins, kind, null);
    }

    private Trending(List<Coin> coins, TrendingKind kind, Date updated) {
        this.coins = coins;
        this.kind = kind;
        this.updated = updated;
    }

    public void saveToCache(Context context) {
        JSONArray data = new JSONArray();
        for (Coin coin : coins) {
            data.put(coin.toJson());
        }

        CacheFileHelper.write(context, getCacheName(kind), data.toString());
    }

    public static Trending restoreFromCache(Context context, TrendingKind kind) {
        String key = getCacheName(kind);
        if (!CacheFileHelper.exists(context, key)) {
            return null;
        }

        long start = System.currentTimeMillis();
        String text = CacheFileHelper.read(context, key);
        if (TextUtils.isEmpty(text)) {
            return null;
        }

        List<Coin> coins = new ArrayList<>();

        try {
            JSONArray data = new JSONArray(text);
            for (int i = 0; i < data.length(); i++) {
                JSONObject attrs = data.getJSONObject(i);
                coins.add(Coin.buildBy(attrs));
            }
        } catch (JSONException e) {
            CKLog.e(TAG, text, e);
        }

        if (DEBUG) CKLog.d(TAG, "restoreFromCache(" + kind.name() + ") elapsed time: "
                + coins.size() + " coins " + (System.currentTimeMillis() - start) + " ms");

        return new Trending(coins, kind, CacheFileHelper.lastModified(context, key));
    }

    private static String getCacheName(TrendingKind kind) {
        return "trending_" + kind.name() + ".json";
    }

    public List<Coin> getCoins() {
        return coins;
    }

    public Date getUpdated() {
        return updated;
    }
}
