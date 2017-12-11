package com.example.toolbartest.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.toolbartest.R;
import com.example.toolbartest.chart.CoinPieChart;
import com.example.toolbartest.cryptocompare.Client;
import com.example.toolbartest.cryptocompare.ClientImpl;
import com.example.toolbartest.cryptocompare.data.CoinSnapshot;
import com.example.toolbartest.cryptocompare.data.Exchange;
import com.example.toolbartest.cryptocompare.data.TopPair;
import com.example.toolbartest.cryptocompare.data.TopPairs;
import com.example.toolbartest.tasks.GetCoinSnapshotTask;
import com.example.toolbartest.tasks.GetTopPairsTask;
import com.example.toolbartest.utils.PrefHelper;
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
        if (taskStarted) {
            return;
        }
        taskStarted = true;
        Client client = new ClientImpl(getActivity(), true);

        if (kind.equals("currency")) {
            new GetTopPairsTask(client).setFromSymbol(fromSymbol)
                    .setListener(this).execute();
        } else if (kind.equals("exchange")) {
            new GetCoinSnapshotTask(client).setFromSymbol(fromSymbol)
                    .setToSymbol(PrefHelper.getToSymbol(getActivity()))
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
        if (topPairs.getTopPairs().isEmpty()) {
            Log.d("finished", "empty, " + kind);
            taskStarted = false;
            startTask();
            return;
        }

        if (isDetached() || getView() == null) {
            return;
        }

        ArrayList<TopPair> pairs = topPairs.getTopPairs();

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
        if (snapshot.getExchanges().isEmpty()) {
            Log.d("finished", "empty, " + kind);
            taskStarted = false;
            startTask();
            return;
        }

        if (isDetached() || getView() == null) {
            return;
        }

        ArrayList<Exchange> exchanges = snapshot.getExchanges();

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