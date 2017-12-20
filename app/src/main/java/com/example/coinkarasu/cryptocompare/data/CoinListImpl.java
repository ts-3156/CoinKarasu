package com.example.coinkarasu.cryptocompare.data;

import android.content.Context;
import android.util.Log;

import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.coins.CoinImpl;
import com.example.coinkarasu.cryptocompare.CoinListReader;
import com.example.coinkarasu.cryptocompare.response.CoinListResponse;
import com.example.coinkarasu.cryptocompare.response.CoinListResponseImpl;
import com.example.coinkarasu.database.AppDatabase;
import com.example.coinkarasu.database.CoinListCoin;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CoinListImpl implements CoinList {

    private static CoinList instance;
    private CoinListResponse response;

    private CoinListImpl(CoinListResponse response) {
        this.response = response;
    }

    public static synchronized CoinList getInstance(Context context) {
        if (instance == null) {
            try {
                instance = CoinListImpl.restoreFromCache(context);
            } catch (Exception e) {
                Log.e("getInstance", e.getMessage());
                try {
                    instance = CoinListImpl.buildByResponse(new JSONObject(CoinListReader.read(context)));
                } catch (JSONException ee) {
                    Log.e("getInstance", ee.getMessage());
                }
            }
        }

        return instance;
    }

    // @Override
    public static CoinList buildByResponse(JSONObject response) {
        return new CoinListImpl(new CoinListResponseImpl(response));
    }

    @Override
    public Coin getCoinBySymbol(String symbol) {
        if (response == null || response.getData() == null) {
            return null;
        }

        Coin coin = null;

        try {
            JSONObject attrs = response.getData().getJSONObject(symbol);
            coin = CoinImpl.buildByAttrs(attrs);
        } catch (JSONException e) {
            Log.e("getCoinBySymbol", e.getMessage());
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
                    coin = CoinImpl.buildByAttrs(attrs);
                    break;
                }
            }
        } catch (JSONException e) {
            Log.d("getCoinByCCId", e.getMessage());
        }

        return coin;
    }

    @Override
    public ArrayList<Coin> collectCoins(String[] fromSymbols) {
        long start = System.currentTimeMillis();
        final ArrayList<Coin> coins = new ArrayList<>(fromSymbols.length);

        for (String coinSymbol : fromSymbols) {
            Coin coin = getCoinBySymbol(coinSymbol);
            if (coin == null) {
                continue;
            }

            coins.add(coin);
        }

        Log.d("collectCoins", "Load from FILE, records " + fromSymbols.length + ", " + (System.currentTimeMillis() - start) + " ms");

        return coins;
    }

    public static ArrayList<Coin> collectCoins(Context context, String[] fromSymbols) {
        long start = System.currentTimeMillis();
        ArrayList<Coin> coins = new ArrayList<>();
        AppDatabase db = AppDatabase.getAppDatabase(context);

        List<CoinListCoin> coinListCoins = db.coinListCoinDao().findBySymbols(fromSymbols);

        for (String symbol : fromSymbols) {
            for (CoinListCoin coinListCoin : coinListCoins) {
                if (coinListCoin.getSymbol().equals(symbol)) {
                    coins.add(CoinImpl.buildByCoinListCoin(coinListCoin));
                }
            }
        }

        Log.d("collectCoins", "Load from DB, records " + fromSymbols.length + ", " + (System.currentTimeMillis() - start) + " ms");

        return coins;
    }

    @Override
    public ArrayList<String> getAllSymbols() {
        return getAllSymbols(0, 3000);
    }

    @Override
    public ArrayList<String> getAllSymbols(int offset, int limit) {
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
    public void removeBySymbols(ArrayList<String> symbols) {
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
    public boolean saveToCache(Context context) {
        return response != null && response.saveToCache(context);
    }

    // @Override
    private static CoinList restoreFromCache(Context context) {
        CoinListResponse coinListResponse = CoinListResponseImpl.restoreFromCache(context);
        if (coinListResponse == null || !coinListResponse.isSuccess()) {
            return null;
        }

        return new CoinListImpl(coinListResponse);
    }
}
