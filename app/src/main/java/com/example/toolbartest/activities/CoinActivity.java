package com.example.toolbartest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.toolbartest.R;
import com.example.toolbartest.bitflyer.data.Board;
import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.coins.CoinImpl;
import com.example.toolbartest.cryptocompare.Client;
import com.example.toolbartest.cryptocompare.ClientImpl;
import com.example.toolbartest.cryptocompare.data.CoinSnapshot;
import com.example.toolbartest.cryptocompare.data.History;
import com.example.toolbartest.cryptocompare.data.TopPairs;
import com.example.toolbartest.format.PriceViewFormat;
import com.example.toolbartest.format.TrendViewFormat;
import com.example.toolbartest.tasks.GetBoardTask;
import com.example.toolbartest.tasks.GetCoinSnapshotTask;
import com.example.toolbartest.tasks.GetHistoryDayTask;
import com.example.toolbartest.tasks.GetHistoryHourTask;
import com.example.toolbartest.tasks.GetHistoryMonthTask;
import com.example.toolbartest.tasks.GetHistoryTaskBase;
import com.example.toolbartest.tasks.GetHistoryWeekTask;
import com.example.toolbartest.tasks.GetHistoryYearTask;
import com.example.toolbartest.tasks.GetTopPairsTask;
import com.example.toolbartest.utils.AnimHelper;
import com.example.toolbartest.utils.AutoUpdateTimer;
import com.example.toolbartest.utils.PrefHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;

public class CoinActivity extends AppCompatActivity implements
        CoinCardFragment.OnFragmentInteractionListener,
        CoinLineChartFragment.OnFragmentInteractionListener,
        CoinPieChartFragment.OnFragmentInteractionListener,
        CoinBoardFragment.OnFragmentInteractionListener,
        CoinLineChartTabContentFragment.OnFragmentInteractionListener {

    private static final String FRAG_CARD = "card_fragment";
    private static final String FRAG_LINE_CHART = "line_chart_fragment";
    private static final String FRAG_PIE_CHART = "pie_chart_fragment";
    private static final String FRAG_BOARD = "board_fragment";

    public static final String COIN_NAME_KEY = "COIN_NAME_KEY";
    public static final String COIN_SYMBOL_KEY = "COIN_SYMBOL_KEY";

    Client client;
    String lineChartKind;
    String pieChartKind;
    String boardKind;
    Coin coin;

    private AutoUpdateTimer autoUpdateTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin);

        Intent intent = getIntent();
        try {
            coin = CoinImpl.buildByJSONObject(new JSONObject(intent.getStringExtra(COIN_NAME_KEY)));
        } catch (JSONException e) {
            Log.d("onCreate", e.getMessage());
        }

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(coin.getFullName());
        }

        client = new ClientImpl(this, true);
        lineChartKind = "hour";
        pieChartKind = "currency";
        boardKind = "order_book";

        Fragment frag1 = CoinCardFragment.newInstance("overview", coin.toJson().toString());
        Fragment frag2 = CoinLineChartFragment.newInstance(lineChartKind, coin.getSymbol(), PrefHelper.getToSymbol(this));
        Fragment frag3 = CoinPieChartFragment.newInstance(pieChartKind);
        Fragment frag4 = CoinBoardFragment.newInstance(boardKind);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.card_overview, frag1, FRAG_CARD)
                .replace(R.id.card_line_chart, frag2, FRAG_LINE_CHART)
                .replace(R.id.card_pie_chart, frag3, FRAG_PIE_CHART)
                .replace(R.id.card_board, frag4, FRAG_BOARD)
                .commit();

        startAutoUpdate(0);
        drawPieChart();
        drawBoardChart();

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

    private void startAutoUpdate(int delay) {
        if (autoUpdateTimer != null) {
            stopAutoUpdate();
        }

        autoUpdateTimer = new AutoUpdateTimer(lineChartKind);

        autoUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
//                drawLineChart();
            }
        }, delay, 60000);
    }

    public void stopAutoUpdate() {
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
            autoUpdateTimer = null;
        }
    }

    private void drawExchangePieChart() {
        new GetCoinSnapshotTask(client).setFromSymbol(coin.getSymbol())
                .setToSymbol(PrefHelper.getToSymbol(this))
                .setListener(new GetCoinSnapshotTask.Listener() {
                    @Override
                    public void finished(CoinSnapshot snapshot) {
                        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAG_PIE_CHART);
                        if (fragment != null) {
                            ((CoinPieChartFragment) fragment).updateView(snapshot);
                            Log.d("UPDATED", pieChartKind + ", " + new Date().toString());
                        }
                    }
                }).execute();
    }

    private void drawCurrencyPieChart() {
        new GetTopPairsTask(client).setFromSymbol(coin.getSymbol())
                .setListener(new GetTopPairsTask.Listener() {
                    @Override
                    public void finished(TopPairs topPairs) {
                        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAG_PIE_CHART);
                        if (fragment != null) {
                            ((CoinPieChartFragment) fragment).updateView(topPairs);
                            Log.d("UPDATED", pieChartKind + ", " + new Date().toString());
                        }
                    }
                }).execute();
    }

    private void drawPieChart() {
        if (pieChartKind.equals("currency")) {
            drawCurrencyPieChart();
        } else if (pieChartKind.equals("exchange")) {
            drawExchangePieChart();
        }
    }

    private void drawBoardChart() {
        new GetBoardTask(this).setListener(new GetBoardTask.Listener() {
            @Override
            public void finished(Board board) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAG_BOARD);
                if (fragment != null) {
                    ((CoinBoardFragment) fragment).updateView(board);
                    Log.d("UPDATED", "board, " + new Date().toString());
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

    @Override
    public void onBoardKindChanged(String kind) {
        boardKind = kind;
    }
}
