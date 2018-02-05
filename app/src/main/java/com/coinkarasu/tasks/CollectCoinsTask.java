package com.coinkarasu.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.activities.etc.Section;
import com.coinkarasu.api.cryptocompare.data.CoinList;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.services.UpdateCoinListIntentService;
import com.coinkarasu.services.data.Toplist;
import com.coinkarasu.utils.CKLog;

import java.util.List;

public class CollectCoinsTask extends AsyncTask<Integer, Integer, List<Coin>> {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "CollectCoinsTask";

    private Context context;
    private Listener listener;
    private NavigationKind kind;
    private Section section;

    public CollectCoinsTask(Context context, NavigationKind kind, Section section) {
        this.context = context;
        this.listener = null;
        this.kind = kind;
        this.section = section;
    }

    @Override
    protected List<Coin> doInBackground(Integer... params) {
        String[] fromSymbols = null;

        if (kind.isToplist()) {
            Toplist toplist = Toplist.restoreFromCache(context, kind);
            if (toplist != null) {
                fromSymbols = toplist.getSymbols();
            }

            if (fromSymbols == null || fromSymbols.length == 0) {
                fromSymbols = context.getResources().getStringArray(kind.symbolsResId);
            }
        } else {
            fromSymbols = context.getResources().getStringArray(section.getSymbolsResId());
        }

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

        if (coins != null && fromSymbols.length != coins.size()) {
            if (DEBUG) CKLog.w(TAG, "doInBackground() "
                    + " Different size " + fromSymbols.length + " symbols " + coins.size() + " coins");
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

    public CollectCoinsTask setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public interface Listener {
        void coinsCollected(List<Coin> coins);
    }
}
