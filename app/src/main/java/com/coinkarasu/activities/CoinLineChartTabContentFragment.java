package com.coinkarasu.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coinkarasu.R;
import com.coinkarasu.activities.HistoricalPriceFragment.HistoricalPriceKind;
import com.coinkarasu.api.cryptocompare.ClientFactory;
import com.coinkarasu.api.cryptocompare.data.History;
import com.coinkarasu.chart.CoinLineChart;
import com.coinkarasu.tasks.GetHistoryTaskBase;
import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;
import java.util.Date;

public class CoinLineChartTabContentFragment extends Fragment implements GetHistoryTaskBase.Listener {

    private HistoricalPriceKind kind;
    private String fromSymbol;
    private String toSymbol;
    private boolean taskStarted;
    private CoinLineChart chart;
    private int errorCount = 0;
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
        chart = null;
        startTask();
        return view;
    }

    private void startTask() {
        if (taskStarted || errorCount >= 3 || getActivity() == null) {
            return;
        }
        taskStarted = true;

        GetHistoryTaskBase.newInstance(ClientFactory.getInstance(getActivity()), kind.name())
                .setFromSymbol(fromSymbol)
                .setToSymbol(toSymbol)
                .setListener(this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void updateView() {
        if (isDetached() || getView() == null) {
            return;
        }

        if (taskStarted) {
            return;
        }

        startTask();
    }

    @Override
    public void finished(ArrayList<History> records) {
        if (records.isEmpty()) {
            Log.e("finished", "empty, " + kind + ", " + errorCount);
            taskStarted = false;
            errorCount++;
            startTask();
            return;
        }

        if (isDetached() || getView() == null) {
            taskStarted = false;
            errorCount++;
            return;
        }

        drawChart(records);
        ((HistoricalPriceFragment) getParentFragment()).updateTab(kind.ordinal(), records);

        Log.d("UPDATED", kind + ", " + records.size() + ", " + new Date().toString());
    }

    private void drawChart(ArrayList<History> records) {
        chart = new CoinLineChart((LineChart) getView().findViewById(R.id.line_chart));
        chart.initialize(kind.name());
        chart.setData(records);
        chart.invalidate();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        kind = null;
        fromSymbol = null;
        toSymbol = null;
        chart = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        // This method may be called outside of the fragment lifecycle.
        this.isVisibleToUser = isVisibleToUser;

        if (isVisibleToUser) {
            updateView();
        }
    }
}