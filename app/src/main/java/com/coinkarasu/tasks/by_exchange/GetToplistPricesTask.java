package com.coinkarasu.tasks.by_exchange;

import android.content.Context;

import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.services.data.Toplist;
import com.coinkarasu.tasks.by_exchange.data.Price;
import com.coinkarasu.utils.CKLog;

import java.util.List;

public class GetToplistPricesTask extends GetCccaggPricesTask {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "GetToplistPricesTask";

    private NavigationKind kind;

    public GetToplistPricesTask(Context context, NavigationKind kind) {
        super(context, kind.exchanges[0]);
        this.kind = kind;

        if (!kind.isToplist()) {
            throw new RuntimeException("Invalid kind " + kind.name());
        }
    }

    @Override
    protected List<Price> doInBackground(Integer... params) {
        Toplist toplist = Toplist.restoreFromCache(context, kind);
        if (toplist != null) {
            fromSymbols = toplist.getSymbols();
        }

        if (fromSymbols == null || fromSymbols.length == 0) {
            fromSymbols = context.getResources().getStringArray(kind.symbolsResId);
        }

        return super.doInBackground(params);
    }

    @Override
    public GetToplistPricesTask setFromSymbols(String[] fromSymbols) {
        throw new UnsupportedOperationException();
    }
}
