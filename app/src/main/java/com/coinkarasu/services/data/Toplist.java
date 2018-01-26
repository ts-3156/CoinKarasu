package com.coinkarasu.services.data;

import android.content.Context;
import android.text.TextUtils;

import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.coins.PriceMultiFullCoin;
import com.coinkarasu.coins.PriceMultiFullCoinImpl;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.io.CacheFileHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Toplist {

    private static final boolean DEBUG = true;
    private static final String TAG = "Toplist";

    private NavigationKind kind;
    private List<PriceMultiFullCoin> coins;
    private Date updated;

    public Toplist(List<PriceMultiFullCoin> coins, NavigationKind kind) {
        this(coins, kind, null);
    }

    private Toplist(List<PriceMultiFullCoin> coins, NavigationKind kind, Date updated) {
        this.coins = coins;
        this.kind = kind;
        this.updated = updated;
    }

    public void saveToCache(Context context) {
        JSONArray data = new JSONArray();
        for (PriceMultiFullCoin coin : coins) {
            data.put(coin.toJson());
        }

        CacheFileHelper.write(context, getCacheName(kind), data.toString());
    }

    public static Toplist restoreFromCache(Context context, NavigationKind kind) {
        String key = getCacheName(kind);
        if (!CacheFileHelper.exists(context, key)) {
            return null;
        }

        long start = System.currentTimeMillis();
        String text = CacheFileHelper.read(context, key);
        if (TextUtils.isEmpty(text)) {
            if (DEBUG) CKLog.w(TAG, "restoreFromCache() " + kind.name() + " cache is null.");
            return null;
        }

        List<PriceMultiFullCoin> coins = new ArrayList<>();

        try {
            JSONArray data = new JSONArray(text);
            for (int i = 0; i < data.length(); i++) {
                JSONObject attrs = data.getJSONObject(i);
                coins.add(new PriceMultiFullCoinImpl(attrs));
            }
        } catch (JSONException e) {
            CKLog.e(TAG, text, e);
        }

        if (DEBUG) CKLog.d(TAG, "restoreFromCache(" + kind.name() + ") elapsed time: "
                + coins.size() + " coins " + (System.currentTimeMillis() - start) + " ms");

        return new Toplist(coins, kind, CacheFileHelper.lastModified(context, key));
    }

    private static String getCacheName(NavigationKind kind) {
        return "toplist_" + kind.name() + ".json";
    }

    public List<PriceMultiFullCoin> getCoins() {
        return coins;
    }

    public String[] getSymbols() {
        List<String> list = new ArrayList<>(coins.size());
        for (PriceMultiFullCoin coin : coins) {
            list.add((coin.getFromSymbol()));
        }
        return list.toArray(new String[list.size()]);
    }

    public Date getUpdated() {
        return updated;
    }
}
