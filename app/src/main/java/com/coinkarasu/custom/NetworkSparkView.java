package com.coinkarasu.custom;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.coinkarasu.adapters.CoinListSparkAdapter;
import com.coinkarasu.api.cryptocompare.ClientFactory;
import com.coinkarasu.api.cryptocompare.data.History;
import com.coinkarasu.tasks.GetHistoryTaskBase;
import com.coinkarasu.tasks.GetHistoryWeekTask;
import com.robinhood.spark.SparkView;

import java.util.List;

public class NetworkSparkView extends SparkView {
    private static final boolean DEBUG = true;
    private static final String TAG = "NetworkSparkView";

    private GetHistoryTaskBase task;
    private String fromSymbol;
    private String toSymbol;
    private boolean isDetached;

    public NetworkSparkView(Context context) {
        this(context, null);
    }

    public NetworkSparkView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NetworkSparkView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setSymbols(String fromSymbol, String toSymbol) {
        this.fromSymbol = fromSymbol;
        this.toSymbol = toSymbol;
        this.isDetached = false;
        load();
    }

    void load() {
        if (TextUtils.isEmpty(fromSymbol) || TextUtils.isEmpty(toSymbol)) {
            if (task != null) {
                task.cancel(false);
                task = null;
            }
            return;
        }

        if (task != null) {
            if (task.getFromSymbol().equals(fromSymbol) && task.getToSymbol().equals(toSymbol)) {
                return;
            } else {
                task.cancel(false);
            }
        }

        task = new GetHistoryWeekTask(ClientFactory.getInstance(getContext()))
                .setFromSymbol(fromSymbol)
                .setToSymbol(toSymbol)
                .setListener(new GetHistoryTaskBase.Listener() {
                    @Override
                    public void finished(List<History> histories) {
                        if (isDetached || task == null) {
                            return;
                        }
                        if (histories == null || histories.isEmpty()) {
                            return;
                        }

                        double[] data = new double[histories.size()];

                        for (int i = 0; i < histories.size(); i++) {
                            data[i] = histories.get(i).getClose();
                        }

                        setAdapter(new CoinListSparkAdapter(data));
                    }
                });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onDetachedFromWindow() {
        isDetached = true;
        if (task != null) {
            task.cancel(false);
            task = null;
        }
        super.onDetachedFromWindow();
    }
}
