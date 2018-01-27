package com.coinkarasu.tasks;

import android.os.AsyncTask;

import com.coinkarasu.activities.CoinActivity;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.database.AppDatabase;
import com.coinkarasu.database.ViewCoinEvent;

public class InsertViewCoinEventTask extends AsyncTask<Object, Void, Void> {
    @Override
    protected Void doInBackground(Object... params) {
        CoinActivity activity = (CoinActivity) params[0];
        NavigationKind kind = (NavigationKind) params[1];
        Coin coin = (Coin) params[2];

        AppDatabase db = AppDatabase.getAppDatabase(activity);
        db.viewCoinEventDao().insertEvent(new ViewCoinEvent(kind, coin));

        return null;
    }
}
