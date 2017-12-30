package com.example.coinkarasu.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.example.coinkarasu.api.cryptocompare.data.CoinListImpl;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.services.UpdateCoinListIntentService;

import java.util.ArrayList;

public class CollectCoinsTask extends AsyncTask<Integer, Integer, Integer> {
    private Listener listener;
    private String[] fromSymbols;
    private Context context;
    private ArrayList<Coin> coins;

    public CollectCoinsTask(Context context) {
        this.context = context;
        this.listener = null;
        this.fromSymbols = null;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        if (fromSymbols.length > 300) {
            coins = CoinListImpl.getInstance(context).collectCoins(fromSymbols); // File
        } else {
            coins = CoinListImpl.collectCoins(context, fromSymbols); // DB

            if (coins == null || coins.size() != fromSymbols.length) {
                coins = CoinListImpl.getInstance(context).collectCoins(fromSymbols);
                UpdateCoinListIntentService.start(context);
            }
        }

        return 200;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        context = null;
        if (listener != null) {
            listener.collected(coins);
        }
    }

    public CollectCoinsTask setFromSymbols(String[] fromSymbols) {
        this.fromSymbols = fromSymbols;
        return this;
    }

    public CollectCoinsTask setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public interface Listener {
        void collected(ArrayList<Coin> coins);
    }
}