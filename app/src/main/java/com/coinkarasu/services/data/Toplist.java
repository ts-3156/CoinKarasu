package com.coinkarasu.services.data;

import android.content.Context;
import android.util.Log;

import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.coins.PriceMultiFullCoin;
import com.coinkarasu.coins.PriceMultiFullCoinImpl;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.DiskCacheHelper;
import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Toplist {

    private static final boolean DEBUG = true;
    private static final String TAG = "Toplist";

    private NavigationKind kind;
    private ArrayList<PriceMultiFullCoin> coins;

    public Toplist(ArrayList<PriceMultiFullCoin> coins, NavigationKind kind) {
        this.coins = coins;
        this.kind = kind;
    }

    public void saveToCache(Context context) {
        JSONArray data = new JSONArray();
        for (PriceMultiFullCoin coin : coins) {
            data.put(coin.toJson());
        }

        DiskCacheHelper.write(context, getCacheName(kind), data.toString());
    }

    public static Toplist restoreFromCache(Context context, NavigationKind kind) {
        String text = DiskCacheHelper.read(context, getCacheName(kind));
        if (text == null) {
            if (DEBUG) CKLog.e(TAG, "The " + kind.name() + " cache is null.");
            return null;
        }

        ArrayList<PriceMultiFullCoin> coins = new ArrayList<>();

        try {
            JSONArray data = new JSONArray(text);
            for (int i = 0; i < data.length(); i++) {
                JSONObject attrs = data.getJSONObject(i);
                coins.add(new PriceMultiFullCoinImpl(attrs));
            }
        } catch (JSONException e) {
            if (DEBUG) CKLog.e(TAG, text, e);
        }

        return new Toplist(coins, kind);
    }

    private static String getCacheName(NavigationKind kind) {
        return "toplist_" + kind.name() + ".json";
    }

    public List<PriceMultiFullCoin> getCoins() {
        return coins;
    }

    public String[] getSymbols() {
        ArrayList<String> list = new ArrayList<>(coins.size());
        for (PriceMultiFullCoin coin : coins) {
            list.add((coin.getFromSymbol()));
        }
        return list.toArray(new String[list.size()]);
    }

    public NavigationKind getKind() {
        return kind;
    }
}
