package com.example.toolbartest.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.toolbartest.R;
import com.example.toolbartest.chart.CoinLineChart;
import com.example.toolbartest.cryptocompare.Client;
import com.example.toolbartest.cryptocompare.ClientImpl;
import com.example.toolbartest.cryptocompare.data.History;
import com.example.toolbartest.tasks.GetHistoryDayTask;
import com.example.toolbartest.tasks.GetHistoryHourTask;
import com.example.toolbartest.tasks.GetHistoryMonthTask;
import com.example.toolbartest.tasks.GetHistoryTaskBase;
import com.example.toolbartest.tasks.GetHistoryWeekTask;
import com.example.toolbartest.tasks.GetHistoryYearTask;
import com.example.toolbartest.timer.AutoUpdateTimer;
import com.example.toolbartest.utils.PrefHelper;
import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;

public class CoinActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String COIN_NAME_KEY = "COIN_NAME_KEY";
    public static final String COIN_SYMBOL_KEY = "COIN_SYMBOL_KEY";

    Client client;
    ArrayList<History> records;
    CoinLineChart chart;
    String kind;
    Button btn;
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

        findViewById(R.id.chart_hour).setOnClickListener(this);
        findViewById(R.id.chart_day).setOnClickListener(this);
        findViewById(R.id.chart_week).setOnClickListener(this);
        findViewById(R.id.chart_month).setOnClickListener(this);
        findViewById(R.id.chart_year).setOnClickListener(this);

        client = new ClientImpl(this);
        kind = "hour";
        btn = findViewById(R.id.chart_hour);
        btn.setBackgroundColor(Color.LTGRAY);
        chart = new CoinLineChart((LineChart) findViewById(R.id.line_chart), kind);
        records = new ArrayList<>();

        initializeLineChart();
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

    private void initializeLineChart() {
        chart.initialize();
        startAutoUpdate(0);
    }

    private void refreshLineChart() {
        if (chart == null) {
            return;
        }

        stopAutoUpdate();
        chart.setKind(kind);
        chart.updateValueFormatter();
        startAutoUpdate(0);
    }

    private GetHistoryTaskBase getTaskInstance() {
        GetHistoryTaskBase instance;

        switch (kind) {
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

        autoUpdateTimer = new AutoUpdateTimer(kind);

        autoUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getTaskInstance().setFromSymbol(coinSymbol)
                        .setToSymbol(PrefHelper.getToSymbol(CoinActivity.this))
                        .setListener(new GetHistoryTaskBase.Listener() {
                            @Override
                            public void finished(ArrayList<History> records) {
                                if (autoUpdateTimer == null || !autoUpdateTimer.getTag().equals(kind)) {
                                    return;
                                }

                                CoinActivity.this.records = records;
                                chart.getChart().clear();
                                chart.getChart().notifyDataSetChanged();
                                chart.setData(records);
                                chart.getChart().notifyDataSetChanged();
                                chart.invalidate();
                                Log.d("UPDATED", kind + ", " + CoinActivity.this.records.size() + ", " + new Date().toString());
                            }
                        }).execute();
            }
        }, delay, 60000);
    }

    public void stopAutoUpdate() {
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
            autoUpdateTimer = null;
        }
    }

    @Override
    public void onClick(View view) {
        String next;
        Button nextBtn;

        switch (view.getId()) {
            case R.id.chart_hour:
                next = "hour";
                nextBtn = findViewById(R.id.chart_hour);
                break;
            case R.id.chart_day:
                next = "day";
                nextBtn = findViewById(R.id.chart_day);
                break;
            case R.id.chart_week:
                next = "week";
                nextBtn = findViewById(R.id.chart_week);
                break;
            case R.id.chart_month:
                next = "month";
                nextBtn = findViewById(R.id.chart_month);
                break;
            case R.id.chart_year:
                next = "year";
                nextBtn = findViewById(R.id.chart_year);
                break;
            default:
                next = "hour";
                nextBtn = btn;
        }

        if (!next.equals(kind)) {
            btn.setBackgroundColor(Color.WHITE);
            nextBtn.setBackgroundColor(Color.LTGRAY);
            btn = nextBtn;
            kind = next;
            refreshLineChart();
        }
    }
}
