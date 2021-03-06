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
import com.coinkarasu.activities.etc.HistoricalPriceKind;
import com.coinkarasu.api.cryptocompare.ClientFactory;
import com.coinkarasu.api.cryptocompare.data.HistoriesCache;
import com.coinkarasu.api.cryptocompare.data.History;
import com.coinkarasu.chart.CoinLineChart;
import com.coinkarasu.coins.SnapshotCoin;
import com.coinkarasu.coins.SnapshotCoinImpl;
import com.coinkarasu.tasks.GetHistoryHourTask;
import com.coinkarasu.tasks.GetHistoryTaskBase;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.CKStringUtils;
import com.coinkarasu.utils.PrefHelper;
import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CoinExchangeTabContentFragment extends Fragment implements GetHistoryHourTask.Listener {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "CoinExchangeTabContentFragment";

    private SnapshotCoin coin;
    private HistoricalPriceKind kind;
    private int position;
    private String exchange;
    private boolean taskStarted;
    private LineChart chartView;
    private TextView warning;
    private View warningContainer;
    private int errorCount = 0;
    private CoinExchangeFragment parent;

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
            kind = HistoricalPriceKind.day;

            try {
                coin = SnapshotCoinImpl.buildByJSONObject(new JSONObject(coinJson));
            } catch (JSONException e) {
                CKLog.e(TAG, e);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_exchange_tab_content, container, false);
        taskStarted = false;
        chartView = view.findViewById(R.id.line_chart);
        warning = view.findViewById(R.id.warn_text);
        warningContainer = view.findViewById(R.id.warn_container);

        parent = (CoinExchangeFragment) getParentFragment();
        if (parent != null && parent.tabsSetupFinished()) {
            startTask();
        }
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

        GetHistoryTaskBase.newInstance(ClientFactory.getInstance(getActivity()), kind, exchange)
                .setFromSymbol(coin.getFromSymbol())
                .setToSymbol(coin.getToSymbol())
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

        if (records == null) {
            if (DEBUG) CKLog.w(TAG, "finished() records is null " + exchange + " error=" + errorCount);
            taskStarted = false;
            errorCount++;
            return;
        }

        if (records.isEmpty()) {
            if (DEBUG) CKLog.w(TAG, "finished() records is empty " + exchange + " error=" + errorCount);
            displayWarning();
            return;
        }

        new HistoriesCache(getActivity()).put(makeCacheKey(), records);

        refreshUi(records);
    }

    private void refreshUi(List<History> records) {
        drawChart(records);
        if (parent != null && parent.tabsSetupFinished()) {
            parent.refreshTabText(position, records);
        }
    }

    private String makeCacheKey() {
        return CKStringUtils.join("_", TAG, kind, exchange, coin.getFromSymbol(), coin.getToSymbol());
    }

    private void drawChart(List<History> records) {
        CoinLineChart chart = new CoinLineChart(chartView);
        chart.initialize(kind.name(), PrefHelper.shouldAnimateCharts(getActivity()));
        chart.setData(records);
        chart.invalidate();
    }

    private void displayWarning() {
        chartView.setVisibility(View.GONE);
        Spanned text = Html.fromHtml(getString(R.string.exchange_warn_each_exchange, coin.getFromSymbol(), coin.getToSymbol(), exchange));
        warning.setText(text);
        warningContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;

        if (isVisibleToUser && parent != null && parent.tabsSetupFinished()) {
            startTask();
        }
    }

    public void onTabsSetupFinished() {
        startTask();
    }
}
