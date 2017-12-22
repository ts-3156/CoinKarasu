package com.example.coinkarasu.activities;

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

import com.example.coinkarasu.R;
import com.example.coinkarasu.api.cryptocompare.Client;
import com.example.coinkarasu.api.cryptocompare.ClientFactory;
import com.example.coinkarasu.api.cryptocompare.data.CoinSnapshot;
import com.example.coinkarasu.api.cryptocompare.data.TopPair;
import com.example.coinkarasu.chart.CoinPieChart;
import com.example.coinkarasu.coins.SnapshotCoin;
import com.example.coinkarasu.tasks.GetCoinSnapshotTask;
import com.example.coinkarasu.tasks.GetTopPairsTask;
import com.github.mikephil.charting.charts.PieChart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

public class CoinPieChartTabContentFragment extends Fragment implements
        GetTopPairsTask.Listener, GetCoinSnapshotTask.Listener {

    public static final double GROUP_SMALL_SLICES_PCT = 0.05;

    private CoinPieChartFragment.Kind kind;
    private String fromSymbol;
    private String toSymbol;
    private boolean taskStarted;
    private CoinPieChart chart;
    private int errorCount = 0;
    private boolean isVisibleToUser = false;

    public CoinPieChartTabContentFragment() {
    }

    public static CoinPieChartTabContentFragment newInstance(CoinPieChartFragment.Kind kind, String fromSymbol, String toSymbol) {
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
            kind = CoinPieChartFragment.Kind.valueOf(getArguments().getString("kind"));
            fromSymbol = getArguments().getString("fromSymbol");
            toSymbol = getArguments().getString("toSymbol");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_pie_chart_tab_content, container, false);
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
        Client client = ClientFactory.getInstance(getActivity());

        if (kind == CoinPieChartFragment.Kind.currency) {
            new GetTopPairsTask(client)
                    .setFromSymbol(fromSymbol)
                    .setListener(this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else if (kind == CoinPieChartFragment.Kind.exchange) {
            new GetCoinSnapshotTask(client)
                    .setFromSymbol(fromSymbol)
                    .setToSymbol(toSymbol)
                    .setListener(this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void updateView() {
        if (isDetached() || getView() == null) {
            return;
        }

        if (taskStarted) {
            if (chart != null) {
//                chart.animateY();
            }
            return;
        }

        startTask();
    }

    @Override
    public void finished(ArrayList<TopPair> pairs) {
        if (isDetached() || getView() == null) {
            taskStarted = false;
            errorCount++;
            return;
        }

        if (pairs == null) {
            Log.e("finished", "null(retry), " + kind + ", " + errorCount);
            taskStarted = false;
            errorCount++;
            startTask();
            return;
        }

        ArrayList<Double> values = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        if (pairs.isEmpty()) {
            Log.e("finished", "empty, " + kind + ", " + errorCount);
            drawChart(values, labels);
            return;
        }

        Collections.sort(pairs, new Comparator<TopPair>() {
            public int compare(TopPair tp1, TopPair tp2) {
                return tp1.getVolume24h() > tp2.getVolume24h() ? -1 : 1;
            }
        });

        for (int i = 0; i < pairs.size(); i++) {
            TopPair pair = pairs.get(i);
            values.add(pair.getVolume24h());
            labels.add(pair.getToSymbol());
        }

        groupSmallSlices(values, labels);
        drawChart(values, labels);

        Log.d("UPDATED", kind + ", " + new Date().toString());
    }

    @Override
    public void finished(CoinSnapshot snapshot) {
        if (isDetached() || getView() == null) {
            taskStarted = false;
            errorCount++;
            return;
        }

        ArrayList<SnapshotCoin> coins = snapshot.getSnapshotCoins();
        if (coins == null) {
            Log.e("finished", "null(retry), " + kind + ", " + errorCount);
            taskStarted = false;
            errorCount++;
            startTask();
            return;
        }

        Iterator<SnapshotCoin> iterator = coins.iterator();
        while (iterator.hasNext()) {
            SnapshotCoin coin = iterator.next();
            if (coin.getVolume24Hour() <= 0.0) {
                iterator.remove();
            }
        }

        ArrayList<Double> values = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        if (coins.isEmpty()) {
            Log.e("finished", "empty, " + kind + ", " + errorCount);
            drawChart(values, labels);
            return;
        }

        Collections.sort(coins, new Comparator<SnapshotCoin>() {
            public int compare(SnapshotCoin c1, SnapshotCoin c2) {
                return c1.getVolume24Hour() > c2.getVolume24Hour() ? -1 : 1;
            }
        });

        for (int i = 0; i < coins.size(); i++) {
            SnapshotCoin coin = coins.get(i);
            values.add(coin.getVolume24Hour());
            labels.add(coin.getMarket());
        }

        groupSmallSlices(values, labels);
        drawChart(values, labels);

        Log.d("UPDATED", kind + ", " + new Date().toString());
    }

    private void drawChart(ArrayList<Double> values, ArrayList<String> labels) {
        if (values.isEmpty()) {
            getView().findViewById(R.id.pie_chart).setVisibility(View.GONE);
            Spanned text = Html.fromHtml(getString(R.string.pie_chart_exchange_warn, fromSymbol, toSymbol));
            ((TextView) getView().findViewById(R.id.warn_text)).setText(text);
            getView().findViewById(R.id.warn).setVisibility(View.VISIBLE);
            return;
        }

        chart = new CoinPieChart((PieChart) getView().findViewById(R.id.pie_chart));
        chart.initialize();

        if (kind == CoinPieChartFragment.Kind.currency) {
            chart.setCurrencyCenterText(getActivity(), fromSymbol);
        } else {
            chart.setExchangeCenterText(getActivity(), fromSymbol, toSymbol);
        }
        chart.setData(values, labels);
        chart.invalidate();
    }

    private void groupSmallSlices(ArrayList<Double> values, ArrayList<String> labels) {
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        double threshold = sum * GROUP_SMALL_SLICES_PCT;

        double others = 0.0;
        ArrayList<Double> newValues = new ArrayList<>();
        ArrayList<String> newLabels = new ArrayList<>();

        for (int i = 0; i < values.size(); i++) {
            double value = values.get(i);
            if (value < threshold) {
                others += value;
            } else {
                newValues.add(value);
                newLabels.add(labels.get(i));
            }
        }

        if (others > 0.0) {
            newValues.add(others);
            newLabels.add("others");
        }

        values.clear();
        values.addAll(newValues);

        labels.clear();
        labels.addAll(newLabels);
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