package com.coinkarasu.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coinkarasu.R;
import com.coinkarasu.activities.etc.HistoricalPriceKind;
import com.coinkarasu.api.cryptocompare.ClientFactory;
import com.coinkarasu.api.cryptocompare.data.HistoriesCache;
import com.coinkarasu.api.cryptocompare.data.History;
import com.coinkarasu.chart.CoinLineChart;
import com.coinkarasu.tasks.GetHistoryTaskBase;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.CKStringUtils;
import com.coinkarasu.utils.PrefHelper;
import com.github.mikephil.charting.charts.LineChart;

import java.util.List;

public class CoinLineChartTabContentFragment extends Fragment implements GetHistoryTaskBase.Listener {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "CoinLineChartTabContentFragment";

    private HistoricalPriceKind kind;
    private String fromSymbol;
    private String toSymbol;
    private boolean taskStarted;
    private int errorCount = 0;
    private LineChart chartView;
    private HistoricalPriceFragment parent;

    private boolean isVisibleToUser = false;

    public CoinLineChartTabContentFragment() {
    }

    public static CoinLineChartTabContentFragment newInstance(HistoricalPriceKind kind, String fromSymbol, String toSymbol) {
        CoinLineChartTabContentFragment fragment = new CoinLineChartTabContentFragment();
        Bundle args = new Bundle();
        args.putString("kind", kind.name());
        args.putString("fromSymbol", fromSymbol);
        args.putString("toSymbol", toSymbol);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            kind = HistoricalPriceKind.valueOf(getArguments().getString("kind"));
            fromSymbol = getArguments().getString("fromSymbol");
            toSymbol = getArguments().getString("toSymbol");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_line_chart_tab_content, container, false);
        taskStarted = false;
        chartView = view.findViewById(R.id.line_chart);
        parent = (HistoricalPriceFragment) getParentFragment();
        startTask();
        return view;
    }

    private void startTask() {
        if (taskStarted || errorCount >= 3 || getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        taskStarted = true;

        List<History> histories = new HistoriesCache(getActivity()).get(makeCacheKey());
        if (histories != null && !histories.isEmpty()) {
            refreshUi(histories);
        }

        if (PrefHelper.isAirplaneModeOn(getActivity())) {
            return;
        }

        GetHistoryTaskBase.newInstance(ClientFactory.getInstance(getActivity()), kind)
                .setFromSymbol(fromSymbol)
                .setToSymbol(toSymbol)
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

        if (records == null || records.isEmpty()) {
            if (DEBUG) CKLog.w(TAG, "finished() records is empty kind=" + kind + " error=" + errorCount);
            taskStarted = false;
            errorCount++;
            return;
        }

        new HistoriesCache(getActivity()).put(makeCacheKey(), records);

        refreshUi(records);
    }

    private void refreshUi(List<History> records) {
        drawChart(records);
        if (parent != null) {
            parent.refreshTabText(kind.ordinal(), records);
        }
    }

    private String makeCacheKey() {
        return CKStringUtils.join("_", TAG, kind, fromSymbol, toSymbol);
    }

    private void drawChart(List<History> records) {
        CoinLineChart chart = new CoinLineChart(chartView);
        chart.initialize(kind.name(), PrefHelper.shouldAnimateCharts(getActivity()));
        chart.setData(records);
        chart.invalidate();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;

        if (isVisibleToUser && parent != null) {
            startTask();
        }
    }
}
