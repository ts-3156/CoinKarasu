package com.coinkarasu.api.cryptocompare.data;

import com.coinkarasu.api.cryptocompare.response.CoinListResponse;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.utils.CKLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CoinListImpl extends CoinList {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "CoinListImpl";

    private CoinListResponse response;

    protected CoinListImpl(CoinListResponse response) {
        this.response = response;
    }

    @Override
    public Coin getCoinBySymbol(String symbol) {
        if (response == null || response.getData() == null) {
            return null;
        }

        Coin coin = null;

        try {
            if (response.getData().has(symbol)) {
                JSONObject attrs = response.getData().getJSONObject(symbol);
                coin = Coin.buildBy(attrs);
            } else {
                if (DEBUG) CKLog.w(TAG, "CoinList doesn't have " + symbol);
            }
        } catch (JSONException e) {
            CKLog.e(TAG, e);
        }

        return coin;
    }

    @Override
    public Coin getCoinByCCId(String id) {
        if (response == null || response.getData() == null) {
            return null;
        }

        Coin coin = null;
        JSONObject data = response.getData();

        try {
            Iterator<String> keys = data.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject attrs = data.getJSONObject(key);
                if (attrs.getString("Id").equals(id)) {
                    coin = Coin.buildBy(attrs);
                    break;
                }
            }
        } catch (JSONException e) {
            CKLog.e(TAG, e);
        }

        return coin;
    }

    @Override
    public List<Coin> collectCoins(String[] fromSymbols) {
        long start = System.currentTimeMillis();
        List<Coin> coins = new ArrayList<>(fromSymbols.length);

        for (String coinSymbol : fromSymbols) {
            Coin coin = getCoinBySymbol(coinSymbol);
            if (coin == null) {
                if (DEBUG) CKLog.w(TAG, "collectCoins() Coin for " + coinSymbol + " is null.");
                continue;
            }

            coins.add(coin);
        }

        if (fromSymbols.length != coins.size()) {
            if (DEBUG) CKLog.w(TAG, "collectCoins() Different size "
                    + fromSymbols.length + " symbols " + coins.size() + " coins from FILE");
        }

        if (DEBUG) CKLog.d(TAG, "collectCoins() from FILE " + coins.size() + " coins "
                + (System.currentTimeMillis() - start) + " ms");

        return coins;
    }

    @Override
    public List<String> getAllSymbols() {
        return getAllSymbols(0, 3000);
    }

    @Override
    public List<String> getAllSymbols(int offset, int limit) {
        if (response == null || response.getData() == null) {
            return null;
        }

        ArrayList<String> symbols = new ArrayList<>();
        Iterator<String> keys = response.getData().keys();
        int i = 0;

        while (keys.hasNext()) {
            String symbol = keys.next();
            if (symbol.contains("*")) {
                continue;
            }

            i++;
            if (i <= offset) {
                continue;
            }

            symbols.add(symbol);
            if (symbols.size() >= limit) {
                break;
            }
        }

        return symbols;
    }

    @Override
    public void removeBySymbols(List<String> symbols) {
        JSONObject data = response.getData();
        if (data == null) {
            return;
        }

        for (String symbol : symbols) {
            data.remove(symbol);
        }

        Iterator<String> iterator = data.keys();
        while (iterator.hasNext()) {
            String symbol = iterator.next();
            if (symbol.contains("*")) {
                iterator.remove();
            }
        }
    }

    @Override
    public String toString() {
        return response.toString();
    }
}
