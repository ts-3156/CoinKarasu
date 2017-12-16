package com.example.coinkarasu.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.coinkarasu.R;
import com.example.coinkarasu.chart.CoinLineChart;
import com.example.coinkarasu.coins.SnapshotCoin;
import com.example.coinkarasu.coins.SnapshotCoinImpl;
import com.example.coinkarasu.cryptocompare.ClientImpl;
import com.example.coinkarasu.cryptocompare.data.History;
import com.example.coinkarasu.tasks.GetHistoryHourTask;
import com.example.coinkarasu.tasks.GetHistoryTaskBase;
import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class CoinExchangeTabContentFragment extends Fragment implements GetHistoryHourTask.Listener {

    private OnFragmentInteractionListener listener;

    private SnapshotCoin coin;
    private String kind;
    private String fromSymbol;
    private String toSymbol;
    private int position;
    private String exchange;
    private boolean taskStarted;
    private CoinLineChart chart = null;
    private int errorCount = 0;

    private ArrayList<History> records;

    public CoinExchangeTabContentFragment() {
    }

    public static CoinExchangeTabContentFragment newInstance(SnapshotCoin coin, String fromSymbol, String toSymbol, int position, String exchange) {
        CoinExchangeTabContentFragment fragment = new CoinExchangeTabContentFragment();
        Bundle args = new Bundle();
        args.putString("coinJson", coin.toJson().toString());
        args.putString("fromSymbol", fromSymbol);
        args.putString("toSymbol", toSymbol);
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
            fromSymbol = getArguments().getString("fromSymbol");
            toSymbol = getArguments().getString("toSymbol");
            position = getArguments().getInt("position");
            exchange = getArguments().getString("exchange");
            kind = "day";

            try {
                coin = SnapshotCoinImpl.buildByJSONObject(new JSONObject(coinJson));
            } catch (JSONException e) {
                Log.e("onCreate", e.getMessage());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_exchange_tab_content, container, false);
        startTask();
        return view;
    }

    private void startTask() {
        if (taskStarted || errorCount >= 3) {
            return;
        }
        taskStarted = true;

        GetHistoryTaskBase.newInstance(new ClientImpl(getActivity()), kind, exchange)
                .setFromSymbol(fromSymbol)
                .setToSymbol(toSymbol)
                .setListener(this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void updateView() {
        if (isDetached() || getView() == null) {
            return;
        }

        if (taskStarted) {
            if (chart != null) {

                // TODO After calling onPause, chart.animateX() does not show anything.
                // chart.animateX();
                chart = new CoinLineChart((LineChart) getView().findViewById(R.id.line_chart));
                chart.initialize(kind);
                chart.setData(records);
                chart.invalidate();
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
        ((CoinExchangeFragment) getParentFragment()).updateTab(position, records);

        Log.d("UPDATED", kind + ", " + records.size() + ", " + new Date().toString());
    }

    private void drawChart(ArrayList<History> records) {
        this.records = records;

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