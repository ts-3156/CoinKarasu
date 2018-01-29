package com.coinkarasu.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.coinkarasu.api.cryptocompare.data.CoinList;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.services.UpdateCoinListIntentService;

import java.util.List;

public class CollectCoinsTask extends AsyncTask<Integer, Integer, List<Coin>> {
    private Listener listener;
    private String[] fromSymbols;
    private Context context;

    public CollectCoinsTask(Context context) {
        this.context = context;
        this.listener = null;
        this.fromSymbols = null;
    }

    @Override
    protected List<Coin> doInBackground(Integer... params) {
        List<Coin> coins;
        if (fromSymbols.length > 300) {
            coins = CoinList.getInstance(context).collectCoins(fromSymbols); // File
        } else {
            coins = CoinList.collectCoins(context, fromSymbols); // DB

            if (coins == null || coins.size() != fromSymbols.length) {
                coins = CoinList.getInstance(context).collectCoins(fromSymbols); // File
                UpdateCoinListIntentService.start(context);
            }
        }

        return coins;
    }

    @Override
    protected void onPostExecute(List<Coin> result) {
        context = null;
        if (listener != null) {
            listener.coinsCollected(result);
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
        void coinsCollected(List<Coin> coins);
    }
}
