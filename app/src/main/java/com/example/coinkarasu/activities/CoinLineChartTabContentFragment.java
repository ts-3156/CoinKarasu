package com.example.coinkarasu.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.coinkarasu.cryptocompare.data.History;
import com.example.coinkarasu.R;
import com.example.coinkarasu.chart.CoinLineChart;
import com.example.coinkarasu.cryptocompare.ClientImpl;
import com.example.coinkarasu.tasks.GetHistoryTaskBase;
import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;
import java.util.Date;

public class CoinLineChartTabContentFragment extends Fragment implements GetHistoryTaskBase.Listener {

    private OnFragmentInteractionListener listener;

    private String kind;
    private String fromSymbol;
    private String toSymbol;
    private int position;
    private boolean taskStarted;
    private CoinLineChart chart = null;
    private int errorCount = 0;

    public CoinLineChartTabContentFragment() {
    }

    public static CoinLineChartTabContentFragment newInstance(String kind, String fromSymbol, String toSymbol, int position) {
        CoinLineChartTabContentFragment fragment = new CoinLineChartTabContentFragment();
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
        View view = inflater.inflate(R.layout.fragment_coin_line_chart_tab_content, container, false);
        startTask();
        return view;
    }

    private void startTask() {
        if (taskStarted || errorCount >= 3) {
            return;
        }
        taskStarted = true;

        GetHistoryTaskBase.newInstance(new ClientImpl(getActivity()), kind)
                .setFromSymbol(fromSymbol)
                .setToSymbol(toSymbol)
                .setListener(CoinLineChartTabContentFragment.this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void updateView(boolean isScroll) {
        if (isDetached() || getView() == null) {
            return;
        }

        if (taskStarted) {
            if (chart != null) {
                chart.animateX();
            }
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
            return;
        }

        drawChart(records);
        ((CoinLineChartFragment) getParentFragment()).updateTab(position, records);

        Log.d("UPDATED", kind + ", " + records.size() + ", " + new Date().toString());
    }

    private void drawChart(ArrayList<History> records) {
        chart = new CoinLineChart((LineChart) getView().findViewById(R.id.line_chart));
        chart.initialize(kind);
        chart.setData(records);
        chart.invalidate();
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