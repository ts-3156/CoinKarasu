package com.coinkarasu.coins;

import com.coinkarasu.utils.CKLog;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Coin implements CoinListCoin, PriceMultiFullCoin, SectionHeaderCoin, AdCoin, UpdatableCoin, TradingOrSalesCoin {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "Coin";

    public static Coin buildBy(JSONObject attrs) {
        return new CoinImpl(attrs);
    }

    public static Coin buildBy(String stringJson) {
        JSONObject json;
        try {
            json = new JSONObject(stringJson);
        } catch (JSONException e) {
            CKLog.e(TAG, stringJson, e);
            return null;
        }
        return new CoinImpl(json);
    }

    public static Coin buildBy(PriceMultiFullCoin coin, String fullName, String imageUrl) {
        JSONObject attrs = new JSONObject();

        try {
            attrs.put("Symbol", coin.getFromSymbol());
            attrs.put("FullName", fullName);
            attrs.put("ImageUrl", imageUrl);
        } catch (JSONException e) {
            CKLog.e(TAG, coin.toJson().toString(), e);
            return null;
        }

        return new CoinImpl(attrs);
    }

    public static Coin buildBy(com.coinkarasu.database.CoinListCoin coin) {
        return new CoinImpl(coin.toJson());
    }

    public abstract String getExchange();

    public abstract boolean isChanged();

    public abstract String toString();

    public abstract JSONObject toJson();
}
