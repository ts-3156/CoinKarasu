package com.example.coinkarasu.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.coinkarasu.R;
import com.example.coinkarasu.chart.CoinPieChart;
import com.example.coinkarasu.cryptocompare.Client;
import com.example.coinkarasu.cryptocompare.ClientImpl;
import com.example.coinkarasu.cryptocompare.data.CoinSnapshot;
import com.example.coinkarasu.cryptocompare.data.Exchange;
import com.example.coinkarasu.cryptocompare.data.TopPair;
import com.example.coinkarasu.cryptocompare.data.TopPairs;
import com.example.coinkarasu.tasks.GetCoinSnapshotTask;
import com.example.coinkarasu.tasks.GetTopPairsTask;
import com.github.mikephil.charting.charts.PieChart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class CoinPieChartTabContentFragment extends Fragment implements
        GetTopPairsTask.Listener, GetCoinSnapshotTask.Listener {

    private OnFragmentInteractionListener listener;

    private String kind;
    private String fromSymbol;
    private String toSymbol;
    private int position;
    private boolean taskStarted;
    private CoinPieChart chart = null;
    private int errorCount = 0;

    public CoinPieChartTabContentFragment() {
    }

    public static CoinPieChartTabContentFragment newInstance(String kind, String fromSymbol, String toSymbol, int position) {
        CoinPieChartTabContentFragment fragment = new CoinPieChartTabContentFragment();
        Bundle args = new Bundle();
        args.putString("kind", kind);
        args.putString("fromSymbol", fromSymbol);
        args.putString("toSymbol", toSymbol);
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            kind = getArguments().getString("kind");
            fromSymbol = getArguments().getString("fromSymbol");
            toSymbol = getArguments().getString("toSymbol");
            position = getArguments().getInt("position");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_pie_chart_tab_content, container, false);
        startTask();

        ((TextView) view.findViewById(R.id.pie_chart_desc)).setText("Volume by " + kind);
        return view;
    }

    private void startTask() {
        if (taskStarted || errorCount >= 3) {
            return;
        }
        taskStarted = true;
        Client client = new ClientImpl(getActivity());

        if (CoinPieChartFragment.Kind.currency == CoinPieChartFragment.Kind.valueOf(kind)) {
            new GetTopPairsTask(client).setFromSymbol(fromSymbol)
                    .setListener(this).execute();
        } else if (CoinPieChartFragment.Kind.exchange == CoinPieChartFragment.Kind.valueOf(kind)) {
            new GetCoinSnapshotTask(client).setFromSymbol(fromSymbol)
                    .setToSymbol(toSymbol)
                    .setListener(this).execute();
        }
    }

    public void updateView(boolean isScroll) {
        if (isDetached() || getView() == null) {
            return;
        }

        if (taskStarted) {
            if (chart != null) {
                chart.animateY();
            }
            return;
        }

        startTask();
    }

    @Override
    public void finished(TopPairs topPairs) {
        ArrayList<TopPair> pairs = topPairs.getTopPairs();
        if (pairs == null) {
            Log.e("finished", "null(retry), " + kind + ", " + errorCount);
            taskStarted = false;
            errorCount++;
            startTask();
            return;
        }

        if (pairs.isEmpty()) {
            Log.e("finished", "empty, " + kind + ", " + errorCount);
            return;
        }

        if (isDetached() || getView() == null) {
            return;
        }

        Collections.sort(pairs, new Comparator<TopPair>() {
            public int compare(TopPair tp1, TopPair tp2) {
                return tp1.getVolume24h() > tp2.getVolume24h() ? -1 : 1;
            }
        });

        ArrayList<Double> values = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < pairs.size(); i++) {
            TopPair pair = pairs.get(i);
            values.add(pair.getVolume24h());
            labels.add(pair.getToSymbol());
        }

        groupSmallSlices(values, labels);
        drawChart(values, labels);
        ((CoinPieChartFragment) getParentFragment()).updateTab(position);

        Log.d("UPDATED", kind + ", " + new Date().toString());
    }

    @Override
    public void finished(CoinSnapshot snapshot) {
        ArrayList<Exchange> exchanges = snapshot.getExchanges();
        if (exchanges == null) {
            Log.e("finished", "null(retry), " + kind + ", " + errorCount);
            taskStarted = false;
            errorCount++;
            startTask();
            return;
        }

        if (exchanges.isEmpty()) {
            Log.e("finished", "empty, " + kind + ", " + errorCount);
            return;
        }

        if (isDetached() || getView() == null) {
            return;
        }

        Collections.sort(exchanges, new Comparator<Exchange>() {
            public int compare(Exchange ex1, Exchange ex2) {
                return ex1.getVolume24Hour() > ex2.getVolume24Hour() ? -1 : 1;
            }
        });

        ArrayList<Double> values = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < exchanges.size(); i++) {
            Exchange exchange = exchanges.get(i);
            values.add(exchange.getVolume24Hour());
            labels.add(exchange.getMarket());
        }

        groupSmallSlices(values, labels);
        drawChart(values, labels);
        ((CoinPieChartFragment) getParentFragment()).updateTab(position);

        Log.d("UPDATED", kind + ", " + new Date().toString());
    }

    private void drawChart(ArrayList<Double> values, ArrayList<String> labels) {
        chart = new CoinPieChart((PieChart) getView().findViewById(R.id.pie_chart));
        chart.initialize();
        chart.setData(values, labels);
        chart.invalidate();
    }

    private void groupSmallSlices(ArrayList<Double> values, ArrayList<String> labels) {
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        double threshold = sum * 0.05;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnFragmentInteractionListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnFragmentInteractionListener {
        void onLineChartKindChanged(String kind);
    }

}