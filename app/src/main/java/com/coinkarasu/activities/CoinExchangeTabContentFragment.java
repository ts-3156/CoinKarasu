package com.coinkarasu.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coinkarasu.R;
import com.coinkarasu.activities.etc.HistoricalPriceKind;
import com.coinkarasu.api.cryptocompare.ClientFactory;
import com.coinkarasu.api.cryptocompare.data.History;
import com.coinkarasu.chart.CoinLineChart;
import com.coinkarasu.coins.SnapshotCoin;
import com.coinkarasu.coins.SnapshotCoinImpl;
import com.coinkarasu.tasks.GetHistoryHourTask;
import com.coinkarasu.tasks.GetHistoryTaskBase;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.PrefHelper;
import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CoinExchangeTabContentFragment extends Fragment implements GetHistoryHourTask.Listener {
    private static final boolean DEBUG = true;
    private static final String TAG = "CoinExchangeTabContentFragment";

    private SnapshotCoin coin;
    private HistoricalPriceKind kind;
    private int position;
    private String exchange;
    private boolean taskStarted;
    private CoinLineChart chart;
    private LineChart chartView;
    private TextView warning;
    private View warningContainer;
    private int errorCount = 0;
    private CoinExchangeFragment parent;

    private boolean isVisibleToUser = false;

    public CoinExchangeTabContentFragment() {
    }

    public static CoinExchangeTabContentFragment newInstance(SnapshotCoin coin, int position, String exchange) {
        CoinExchangeTabContentFragment fragment = new CoinExchangeTabContentFragment();
        Bundle args = new Bundle();
        args.putString("coinJson", coin.toJson().toString());
        args.putInt("position", position);
        args.putString("exchange", exchange);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String coinJson = getArguments().getString("coinJson");
            position = getArguments().getInt("position");
            exchange = getArguments().getString("exchange");
            kind = HistoricalPriceKind.day;

            try {
                coin = SnapshotCoinImpl.buildByJSONObject(new JSONObject(coinJson));
            } catch (JSONException e) {
                CKLog.e(TAG, e);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_exchange_tab_content, container, false);
        taskStarted = false;
        chart = null;
        chartView = view.findViewById(R.id.line_chart);
        warning = view.findViewById(R.id.warn_text);
        warningContainer = view.findViewById(R.id.warn_container);
        parent = ((CoinExchangeFragment) getParentFragment());
        startTask();
        return view;
    }

    private void startTask() {
        if (taskStarted || errorCount >= 3 || getActivity() == null || getActivity().isFinishing()) {
            if (DEBUG) CKLog.w(TAG, "startTask() Return started=" + taskStarted + " error=" + errorCount);
            return;
        }
        taskStarted = true;

        GetHistoryTaskBase.newInstance(ClientFactory.getInstance(getActivity()), kind, exchange)
                .setFromSymbol(coin.getFromSymbol())
                .setToSymbol(coin.getToSymbol())
                .setListener(this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void finished(List<History> records) {
        if (getActivity() == null || getActivity().isFinishing() || isDetached() || !isAdded()) {
            if (DEBUG) CKLog.w(TAG, "finished() Too early");
            taskStarted = false;
            return;
        }

        if (records == null) {
            if (DEBUG) CKLog.w(TAG, "finished() records is null " + exchange + " error=" + errorCount);
            taskStarted = false;
            errorCount++;
            startTask();
            return;
        }

        if (records.isEmpty()) {
            if (DEBUG) CKLog.w(TAG, "finished() records is empty " + exchange + " error=" + errorCount);
            displayWarning();
            return;
        }

        drawChart(records);
        if (parent != null) {
            parent.updateTab(position, records);
        }

        if (DEBUG) CKLog.d(TAG, "finished() " + exchange + " " + records.size());
    }

    private void drawChart(List<History> records) {
        chart = new CoinLineChart(chartView);
        chart.initialize(kind.name(), PrefHelper.shouldAnimateCharts(getActivity()));
        chart.setData(records);
        chart.invalidate();
    }

    private void displayWarning() {
        chartView.setVisibility(View.GONE);
        Spanned text = Html.fromHtml(getString(R.string.exchange_warn_each_exchange, coin.getFromSymbol(), coin.getToSymbol(), exchange));
        warning.setText(text);
        warningContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        coin = null;
        kind = null;
        exchange = null;
        chart = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        // This method may be called outside of the fragment lifecycle.
        this.isVisibleToUser = isVisibleToUser;

        if (isVisibleToUser) {
            startTask();
        }
    }
}