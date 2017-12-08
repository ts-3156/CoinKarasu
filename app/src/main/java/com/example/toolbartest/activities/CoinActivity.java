package com.example.toolbartest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.toolbartest.R;
import com.example.toolbartest.cryptocompare.Client;
import com.example.toolbartest.cryptocompare.ClientImpl;
import com.example.toolbartest.cryptocompare.data.CoinSnapshot;
import com.example.toolbartest.cryptocompare.data.History;
import com.example.toolbartest.tasks.GetCoinSnapshotTask;
import com.example.toolbartest.tasks.GetHistoryDayTask;
import com.example.toolbartest.tasks.GetHistoryHourTask;
import com.example.toolbartest.tasks.GetHistoryMonthTask;
import com.example.toolbartest.tasks.GetHistoryTaskBase;
import com.example.toolbartest.tasks.GetHistoryWeekTask;
import com.example.toolbartest.tasks.GetHistoryYearTask;
import com.example.toolbartest.timer.AutoUpdateTimer;
import com.example.toolbartest.utils.PrefHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;

public class CoinActivity extends AppCompatActivity
        implements CoinLineChartFragment.OnFragmentInteractionListener, CoinPieChartFragment.OnFragmentInteractionListener {

    public static final String COIN_NAME_KEY = "COIN_NAME_KEY";
    public static final String COIN_SYMBOL_KEY = "COIN_SYMBOL_KEY";

    Client client;
    String lineChartKind;
    String pieChartKind;
    String coinSymbol;

    private AutoUpdateTimer autoUpdateTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin);

        Intent intent = getIntent();
        String coinName = intent.getStringExtra(COIN_NAME_KEY);
        coinSymbol = intent.getStringExtra(COIN_SYMBOL_KEY);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(coinName);
            bar.setSubtitle(coinSymbol);
        }

        client = new ClientImpl(this);
        lineChartKind = "hour";
        pieChartKind = "currency";

        CoinLineChartFragment frag1 = CoinLineChartFragment.newInstance(lineChartKind);
        CoinPieChartFragment frag2 = CoinPieChartFragment.newInstance(pieChartKind);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.line_chart_container, frag1, "line_chart_fragment")
                .replace(R.id.pie_chart_container, frag2, "pie_chart_fragment")
                .commit();

        drawPieChart();
        startAutoUpdate(0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (autoUpdateTimer == null) {
            startAutoUpdate(0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoUpdate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAutoUpdate();
    }

    private GetHistoryTaskBase getTaskInstance() {
        GetHistoryTaskBase instance;

        switch (lineChartKind) {
            case "hour":
                instance = new GetHistoryHourTask(client);
                break;
            case "day":
                instance = new GetHistoryDayTask(client);
                break;
            case "week":
                instance = new GetHistoryWeekTask(client);
                break;
            case "month":
                instance = new GetHistoryMonthTask(client);
                break;
            case "year":
                instance = new GetHistoryYearTask(client);
                break;
            default:
                instance = new GetHistoryHourTask(client);
        }

        return instance;
    }

    private void startAutoUpdate(int delay) {
        if (autoUpdateTimer != null) {
            stopAutoUpdate();
        }

        autoUpdateTimer = new AutoUpdateTimer(lineChartKind);

        autoUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                drawLineChart();
            }
        }, delay, 60000);
    }

    public void stopAutoUpdate() {
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
            autoUpdateTimer = null;
        }
    }

    private void drawLineChart() {
        getTaskInstance().setFromSymbol(coinSymbol)
                .setToSymbol(PrefHelper.getToSymbol(this))
                .setListener(new GetHistoryTaskBase.Listener() {
                    @Override
                    public void finished(ArrayList<History> records) {
                        if (autoUpdateTimer == null || !autoUpdateTimer.getTag().equals(lineChartKind)) {
                            return;
                        }

                        Fragment fragment = getSupportFragmentManager().findFragmentByTag("line_chart_fragment");
                        if (fragment != null) {
                            ((CoinLineChartFragment) fragment).updateView(records);
                            Log.d("UPDATED", lineChartKind + ", " + records.size() + ", " + new Date().toString());
                        }
                    }
                }).execute();
    }

    private void drawPieChart() {
        new GetCoinSnapshotTask(client).setFromSymbol(coinSymbol)
                .setToSymbol(PrefHelper.getToSymbol(this))
                .setListener(new GetCoinSnapshotTask.Listener() {
                    @Override
                    public void finished(CoinSnapshot snapshot) {
                        Fragment fragment = getSupportFragmentManager().findFragmentByTag("pie_chart_fragment");
                        if (fragment != null) {
                            ((CoinPieChartFragment) fragment).updateView(snapshot);
                            Log.d("UPDATED", pieChartKind + ", " + new Date().toString());
                        }
                    }
                }).execute();
    }

    @Override
    public void onLineChartKindChanged(String kind) {
        lineChartKind = kind;
        stopAutoUpdate();
        startAutoUpdate(0);
    }

    @Override
    public void onPieChartKindChanged(String kind) {
        pieChartKind = kind;
        drawPieChart();
    }
}
