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
import com.coinkarasu.activities.etc.PieChartKind;
import com.coinkarasu.api.cryptocompare.Client;
import com.coinkarasu.api.cryptocompare.ClientFactory;
import com.coinkarasu.api.cryptocompare.data.CoinSnapshot;
import com.coinkarasu.api.cryptocompare.data.TopPairs;
import com.coinkarasu.chart.CoinPieChart;
import com.coinkarasu.chart.EntriesCache;
import com.coinkarasu.chart.Entry;
import com.coinkarasu.coins.SnapshotCoin;
import com.coinkarasu.coins.TopPairCoin;
import com.coinkarasu.tasks.GetCoinSnapshotTask;
import com.coinkarasu.tasks.GetTopPairsTask;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.PrefHelper;
import com.github.mikephil.charting.charts.PieChart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class CoinPieChartTabContentFragment extends Fragment implements
        GetTopPairsTask.Listener, GetCoinSnapshotTask.Listener {

    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "CoinPieChartTabContentFragment";

    private PieChartKind kind;
    private String fromSymbol;
    private String toSymbol;
    private boolean taskStarted;
    private PieChart chartView;
    private int errorCount = 0;
    private TextView warning;
    private View warningContainer;
    private CoinPieChartFragment parent;

    private boolean isVisibleToUser = false;

    public CoinPieChartTabContentFragment() {
    }

    public static CoinPieChartTabContentFragment newInstance(PieChartKind kind, String fromSymbol, String toSymbol) {
        CoinPieChartTabContentFragment fragment = new CoinPieChartTabContentFragment();
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
            kind = PieChartKind.valueOf(getArguments().getString("kind"));
            fromSymbol = getArguments().getString("fromSymbol");
            toSymbol = getArguments().getString("toSymbol");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_pie_chart_tab_content, container, false);
        taskStarted = false;
        chartView = view.findViewById(R.id.pie_chart);
        warning = view.findViewById(R.id.warn_text);
        warningContainer = view.findViewById(R.id.warn_container);
        parent = (CoinPieChartFragment) getParentFragment();
        startTask();
        return view;
    }

    private void startTask() {
        if (taskStarted || errorCount >= 3 || getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        taskStarted = true;

        List<Entry> entries = new EntriesCache(getActivity()).get(kind, fromSymbol, toSymbol);
        if (entries != null && !entries.isEmpty()) {
            drawChart(entries);
        }

        if (PrefHelper.isAirplaneModeOn(getActivity())) {
            return;
        }

        Client client = ClientFactory.getInstance(getActivity());

        if (kind == PieChartKind.currency) {
            new GetTopPairsTask(client)
                    .setFromSymbol(fromSymbol)
                    .setListener(this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else if (kind == PieChartKind.exchange) {
            new GetCoinSnapshotTask(client)
                    .setFromSymbol(fromSymbol)
                    .setToSymbol(toSymbol)
                    .setListener(this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void finished(TopPairs topPairs) {
        if (getActivity() == null || getActivity().isFinishing() || isDetached() || !isAdded()) {
            if (DEBUG) CKLog.w(TAG, "finished() Too early");
            taskStarted = false;
            return;
        }

        List<TopPairCoin> coins = topPairs.getTopPairCoins();
        if (coins == null) {
            if (DEBUG) CKLog.w(TAG, "finished() null(retry) " + kind + " " + errorCount);
            taskStarted = false;
            errorCount++;
            return;
        }

        if (coins.isEmpty()) {
            if (DEBUG) CKLog.w(TAG, "finished() empty " + kind + " " + errorCount);
            displayWarning();
            return;
        }

        Collections.sort(coins, new Comparator<TopPairCoin>() {
            public int compare(TopPairCoin c1, TopPairCoin c2) {
                return c1.getVolume24h() > c2.getVolume24h() ? -1 : 1;
            }
        });

        List<Entry> entries = new ArrayList<>(coins.size());

        for (int i = 0; i < coins.size(); i++) {
            TopPairCoin coin = coins.get(i);
            entries.add(new Entry(coin.getVolume24h(), coin.getToSymbol()));
        }

        drawChart(entries);
        new EntriesCache(getContext()).put(kind, fromSymbol, toSymbol, entries);
    }

    @Override
    public void finished(CoinSnapshot snapshot) {
        if (getActivity() == null || getActivity().isFinishing() || isDetached() || !isAdded()) {
            if (DEBUG) CKLog.w(TAG, "finished() Too early");
            taskStarted = false;
            return;
        }

        List<SnapshotCoin> coins = snapshot.getSnapshotCoins();
        if (coins == null) {
            if (DEBUG) CKLog.w(TAG, "finished() null(retry) " + kind + " " + errorCount);
            taskStarted = false;
            errorCount++;
            return;
        }

        Iterator<SnapshotCoin> iterator = coins.iterator();
        while (iterator.hasNext()) {
            SnapshotCoin coin = iterator.next();
            if (coin.getVolume24Hour() <= 0.0) {
                iterator.remove();
            }
        }

        if (coins.isEmpty()) {
            if (DEBUG) CKLog.w(TAG, "finished() empty " + kind + " " + errorCount);
            displayWarning();
            return;
        }

        Collections.sort(coins, new Comparator<SnapshotCoin>() {
            public int compare(SnapshotCoin c1, SnapshotCoin c2) {
                return c1.getVolume24Hour() > c2.getVolume24Hour() ? -1 : 1;
            }
        });

        List<Entry> entries = new ArrayList<>(coins.size());

        for (int i = 0; i < coins.size(); i++) {
            SnapshotCoin coin = coins.get(i);
            entries.add(new Entry(coin.getVolume24Hour(), coin.getMarket()));
        }

        drawChart(entries);
        new EntriesCache(getContext()).put(kind, fromSymbol, toSymbol, entries);
    }

    private void drawChart(List<Entry> entries) {
        entries = CoinPieChart.groupSmallSlices(entries);
        CoinPieChart chart = new CoinPieChart(chartView);
        chart.initialize(PrefHelper.shouldAnimateCharts(getActivity()));

        if (kind == PieChartKind.currency) {
            chart.setCurrencyCenterText(getActivity(), fromSymbol);
        } else {
            chart.setExchangeCenterText(getActivity(), fromSymbol, toSymbol);
        }
        chart.setData(entries);
        chart.invalidate();
    }

    private void displayWarning() {
        chartView.setVisibility(View.GONE);
        Spanned text = Html.fromHtml(getString(R.string.pie_chart_exchange_warn, fromSymbol, toSymbol));
        warning.setText(text);
        warningContainer.setVisibility(View.VISIBLE);
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
