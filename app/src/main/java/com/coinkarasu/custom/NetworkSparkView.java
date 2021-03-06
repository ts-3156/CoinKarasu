package com.coinkarasu.custom;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.coinkarasu.activities.etc.HistoricalPriceKind;
import com.coinkarasu.adapters.CoinListSparkAdapter;
import com.coinkarasu.adapters.Configurations;
import com.coinkarasu.api.cryptocompare.CacheMode;
import com.coinkarasu.api.cryptocompare.ClientFactory;
import com.coinkarasu.api.cryptocompare.data.History;
import com.coinkarasu.tasks.GetHistoryTaskBase;
import com.coinkarasu.utils.CKLog;
import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;

import java.util.List;

public class NetworkSparkView extends SparkView implements GetHistoryTaskBase.Listener {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "NetworkSparkView";
    private static final HistoricalPriceKind DEFAULT_KIND = HistoricalPriceKind.day;

    private static final SparkAdapter emptyAdapter = new SparkAdapter() {
        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int index) {
            return null;
        }

        @Override
        public float getY(int index) {
            return 0;
        }
    };

    private GetHistoryTaskBase task;
    private String fromSymbol;
    private String toSymbol;
    private HistoricalPriceKind kind;
    private Configurations configs;

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
        if (kind == null) {
            this.kind = DEFAULT_KIND;
        }
        loadDataIfNecessary();
    }

    private void cancelTask() {
        if (task != null) {
            task.cancel(false);
            task = null;
        }
    }

    public void clearData() {
        cancelTask();
        if (getAdapter() != null) {
            setAdapter(emptyAdapter);
            setAdapter(null);
        }
    }

    private void loadDataIfNecessary() {
        if (TextUtils.isEmpty(fromSymbol) || TextUtils.isEmpty(toSymbol)) {
            cancelTask();
            return;
        }

        if (task != null) {
            if (task.getFromSymbol().equals(fromSymbol) && task.getToSymbol().equals(toSymbol)) {
                return;
            } else {
                cancelTask();
            }
        }

        task = GetHistoryTaskBase.newInstance(ClientFactory.getInstance(getContext()), kind)
                .setFromSymbol(fromSymbol)
                .setToSymbol(toSymbol)
                .setCacheMode(CacheMode.READ_ONLY | CacheMode.IGNORE_EXPIRES)
                .setListener(this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void finished(List<History> histories) {
        if (task == null) {
            return;
        }

        CoinListSparkAdapter adapter = null;

        if (histories == null || histories.isEmpty()) {
            if (DEBUG) CKLog.d(TAG, "finished() histories is null " + fromSymbol + " " + toSymbol);
        } else {
            double[] data = new double[histories.size()];

            for (int i = 0; i < histories.size(); i++) {
                data[i] = histories.get(i).getClose();
            }

            adapter = new CoinListSparkAdapter(data);
        }

        if (getAdapter() == null && (configs != null && !configs.isAirplaneModeOn)) {
            task = GetHistoryTaskBase.newInstance(ClientFactory.getInstance(getContext()), kind)
                    .setFromSymbol(fromSymbol)
                    .setToSymbol(toSymbol)
                    .setCacheMode(CacheMode.FORCE_IF_EXPIRED)
                    .setListener(this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        if (adapter != null) {
            setAdapter(adapter);
        }
    }

    public void setConfigurations(Configurations configs) {
        this.configs = configs;
    }

    public void setKind(HistoricalPriceKind kind) {
        this.kind = kind;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        loadDataIfNecessary();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelTask();
    }
}
