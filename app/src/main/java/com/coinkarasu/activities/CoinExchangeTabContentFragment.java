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
import com.coinkarasu.coins.SnapshotCoin;
import com.coinkarasu.coins.SnapshotCoinImpl;
import com.coinkarasu.tasks.GetHistoryHourTask;
import com.coinkarasu.tasks.GetHistoryTaskBase;
import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CoinExchangeTabContentFragment extends Fragment implements GetHistoryHourTask.Listener {

    private SnapshotCoin coin;
    private String kind;
    private int position;
    private String exchange;
    private boolean taskStarted;
    private CoinLineChart chart;
    private int errorCount = 0;

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

        GetHistoryTaskBase.newInstance(ClientFactory.getInstance(getActivity()), kind, exchange)
                .setFromSymbol(coin.getFromSymbol())
                .setToSymbol(coin.getToSymbol())
                .setListener(this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void updateView() {
        if (isDetached() || getView() == null) {
            return;
        }

        if (taskStarted) {
            if (chart != null) {
                // TODO After calling onPause, chart.animateX() does not show anything.
                // chart.animateX();
//                chart = new CoinLineChart((LineChart) getView().findViewById(R.id.line_chart));
//                chart.initialize(kind);
//                chart.setData(records);
//                chart.invalidate();
            }
            return;
        }

        startTask();
    }

    @Override
    public void finished(List<History> records) {
        if (isDetached() || getView() == null) {
            taskStarted = false;
            errorCount++;
            return;
        }

        if (records == null) {
            Log.e("finished", "null(retry), " + exchange + ", " + errorCount);
            taskStarted = false;
            errorCount++;
            startTask();
            return;
        }

        if (records.isEmpty()) {
            Log.e("finished", "empty, " + exchange + ", " + errorCount);
            drawChart(new ArrayList<History>());
            return;
        }

        drawChart(records);
        ((CoinExchangeFragment) getParentFragment()).updateTab(position, records);

        Log.d("UPDATED", exchange + ", " + records.size() + ", " + new Date().toString());
    }

    private void drawChart(List<History> records) {
        if (records.isEmpty()) {
            getView().findViewById(R.id.line_chart).setVisibility(View.GONE);
            Spanned text = Html.fromHtml(getString(R.string.exchange_warn_each_exchange, coin.getFromSymbol(), coin.getToSymbol(), exchange));
            ((TextView) getView().findViewById(R.id.warn_text)).setText(text);
            getView().findViewById(R.id.warn_container).setVisibility(View.VISIBLE);
            return;
        }

        chart = new CoinLineChart((LineChart) getView().findViewById(R.id.line_chart));
        chart.initialize(kind);
        chart.setData(records);
        chart.invalidate();
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
            updateView();
        }
    }
}