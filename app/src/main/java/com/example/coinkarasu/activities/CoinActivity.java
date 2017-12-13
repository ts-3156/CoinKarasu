package com.example.coinkarasu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.coinkarasu.bitflyer.data.Board;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.coins.CoinImpl;
import com.example.coinkarasu.cryptocompare.Client;
import com.example.coinkarasu.R;
import com.example.coinkarasu.cryptocompare.ClientImpl;
import com.example.coinkarasu.tasks.GetBoardTask;
import com.example.coinkarasu.utils.AutoUpdateTimer;
import com.example.coinkarasu.utils.PrefHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.TimerTask;

public class CoinActivity extends AppCompatActivity implements
        CoinCardFragment.OnFragmentInteractionListener,
        CoinLineChartFragment.OnFragmentInteractionListener,
        CoinPieChartFragment.OnFragmentInteractionListener,
        CoinBoardFragment.OnFragmentInteractionListener,
        CoinLineChartTabContentFragment.OnFragmentInteractionListener,
        CoinPieChartTabContentFragment.OnFragmentInteractionListener {

    private static final String FRAG_CARD = "card_fragment";
    private static final String FRAG_LINE_CHART = "line_chart_fragment";
    private static final String FRAG_PIE_CHART = "pie_chart_fragment";
    private static final String FRAG_BOARD = "board_fragment";

    public static final String COIN_NAME_KEY = "COIN_NAME_KEY";
    public static final String COIN_SYMBOL_KEY = "COIN_SYMBOL_KEY";

    Client client;
    String lineChartKind;
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
            bar.setTitle(coin.getSymbol() + " - " + coin.getToSymbol());
        }

        client = new ClientImpl(this);
        boardKind = "order_book";

        Fragment frag1 = CoinCardFragment.newInstance("overview", coin.toJson().toString());
        Fragment frag2 = CoinLineChartFragment.newInstance(coin.getSymbol(), PrefHelper.getToSymbol(this));
        Fragment frag3 = CoinPieChartFragment.newInstance(coin.getSymbol(), PrefHelper.getToSymbol(this));
        Fragment frag4 = CoinBoardFragment.newInstance(boardKind);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.card_overview, frag1, FRAG_CARD)
                .replace(R.id.card_line_chart, frag2, FRAG_LINE_CHART)
                .replace(R.id.card_pie_chart, frag3, FRAG_PIE_CHART)
                .replace(R.id.card_board, frag4, FRAG_BOARD)
                .commit();

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
            }
        }, delay, 60000);
    }

    public void stopAutoUpdate() {
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
            autoUpdateTimer = null;
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
        stopAutoUpdate();
        startAutoUpdate(0);
    }

    @Override
    public void onPieChartKindChanged(String kind) {
    }

    @Override
    public void onBoardKindChanged(String kind) {
        boardKind = kind;
    }
}