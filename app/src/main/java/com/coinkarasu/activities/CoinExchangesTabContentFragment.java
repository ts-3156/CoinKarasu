package com.coinkarasu.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coinkarasu.R;
import com.coinkarasu.api.cryptocompare.ClientFactory;
import com.coinkarasu.api.cryptocompare.data.History;
import com.coinkarasu.chart.CoinLineChart;
import com.coinkarasu.tasks.GetMultipleHistoryDayTask;
import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CoinExchangesTabContentFragment extends Fragment implements GetMultipleHistoryDayTask.Listener {

    private String kind;
    private String fromSymbol;
    private String toSymbol;
    private int position;
    private String[] exchanges;
    private boolean taskStarted;
    private CoinLineChart chart;
    private int errorCount = 0;

    private boolean isVisibleToUser = false;

    public CoinExchangesTabContentFragment() {
    }

    public static CoinExchangesTabContentFragment newInstance(String fromSymbol, String toSymbol, int position, String[] exchanges) {
        CoinExchangesTabContentFragment fragment = new CoinExchangesTabContentFragment();
        Bundle args = new Bundle();
        args.putString("fromSymbol", fromSymbol);
        args.putString("toSymbol", toSymbol);
        args.putInt("position", position);
        args.putStringArray("exchanges", exchanges);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fromSymbol = getArguments().getString("fromSymbol");
            toSymbol = getArguments().getString("toSymbol");
            position = getArguments().getInt("position");
            exchanges = getArguments().getStringArray("exchanges");
            kind = "day";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_exchange_tab_content, container, false);
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

        new GetMultipleHistoryDayTask(ClientFactory.getInstance(getActivity()))
                .setFromSymbol(fromSymbol)
                .setToSymbol(toSymbol)
                .setExchanges(exchanges)
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
    public void finished(HashMap<String, ArrayList<History>> map) {
        if (isDetached() || getView() == null) {
            taskStarted = false;
            errorCount++;
            return;
        }

        if (map == null) {
            Log.e("finished", "null(retry), " + errorCount);
            taskStarted = false;
            errorCount++;
            startTask();
            return;
        }

        if (map.isEmpty()) {
            Log.e("finished", "empty, " + errorCount);
            drawChart(new HashMap<String, ArrayList<History>>());
            return;
        }

        drawChart(map);
//        ((CoinExchangeFragment) getParentFragment()).updateTab(position, records);

        Log.d("UPDATED", map.size() + ", " + new Date().toString());
    }

    private void drawChart(HashMap<String, ArrayList<History>> map) {
        if (map.isEmpty()) {
            getView().findViewById(R.id.line_chart).setVisibility(View.GONE);
            Spanned text = Html.fromHtml(getString(R.string.exchange_warn_each_exchange, fromSymbol, toSymbol, "")); // TODO
            ((TextView) getView().findViewById(R.id.warn_text)).setText(text);
            getView().findViewById(R.id.warn_container).setVisibility(View.VISIBLE);
            return;
        }

        chart = new CoinLineChart((LineChart) getView().findViewById(R.id.line_chart));
        chart.initialize(kind);
        chart.setData(map);
        chart.invalidate();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        kind = null;
        chart = null;
        fromSymbol = null;
        toSymbol = null;
        exchanges = null;
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