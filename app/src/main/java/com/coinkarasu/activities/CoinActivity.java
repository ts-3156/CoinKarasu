package com.coinkarasu.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import com.coinkarasu.R;
import com.coinkarasu.api.bitflyer.data.Board;
import com.coinkarasu.api.cryptocompare.Client;
import com.coinkarasu.api.cryptocompare.ClientFactory;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.coins.CoinImpl;
import com.coinkarasu.tasks.GetBoardTask;
import com.coinkarasu.utils.PrefHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class CoinActivity extends AppCompatActivity {

    public enum Tag {card, line, exchange, pie, board}

    public static final String KEY_COIN_JSON = "KEY_COIN_JSON";

    Client client;
    String boardKind;
    Coin coin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin);

        Intent intent = getIntent();
        try {
            coin = CoinImpl.buildByAttrs(new JSONObject(intent.getStringExtra(KEY_COIN_JSON)));
        } catch (JSONException e) {
            Log.d("onCreate", e.getMessage());
        }

        client = ClientFactory.getInstance(this);
        boardKind = "order_book";
        updateView();
        drawBoardChart();
    }

    private void updateView() {
        String toSymbol = coin.getToSymbol();
        updateToolbarTitle(toSymbol);

        Fragment card = CoinCardFragment.newInstance(coin);
        Fragment lineChart = CoinLineChartFragment.newInstance(coin.getSymbol(), toSymbol);
        Fragment exchange = CoinExchangeFragment.newInstance("overview", coin.toJson().toString());
        Fragment pieChart = CoinPieChartFragment.newInstance(coin.getSymbol(), toSymbol);
        Fragment board = CoinBoardFragment.newInstance(boardKind);

        setEnterTransition(card);
        setEnterTransition(lineChart);
        setEnterTransition(exchange);
        setEnterTransition(pieChart);
        setEnterTransition(board);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.card_overview, card, Tag.card.name())
                .replace(R.id.card_line_chart, lineChart, Tag.line.name())
                .replace(R.id.card_exchange, exchange, Tag.exchange.name())
                .replace(R.id.card_pie_chart, pieChart, Tag.pie.name())
                .replace(R.id.card_board, board, Tag.board.name())
                .commit();
    }

    private void setEnterTransition(Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide slideToRight = new Slide();
            slideToRight.setSlideEdge(Gravity.RIGHT);

            TransitionSet transition = new TransitionSet();
            transition.addTransition(new Fade());
            transition.addTransition(slideToRight);
            transition.setDuration(700);

            fragment.setEnterTransition(transition);
        }
    }

    private void setExitTransition(Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide slideToLeft = new Slide();
            slideToLeft.setSlideEdge(Gravity.LEFT);

            TransitionSet transition = new TransitionSet();
            transition.addTransition(new Fade());
            transition.addTransition(slideToLeft);
            transition.setDuration(700);

            fragment.setExitTransition(transition);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.coin, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        switchCurrencyMenuTitle(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_currency) {
            if (item.getTitle().toString().equals(getString(MainActivity.Currency.USD.titleStrResId))) {
                PrefHelper.saveToSymbol(this, MainActivity.Currency.JPY.name());
            } else {
                PrefHelper.saveToSymbol(this, MainActivity.Currency.USD.name());
            }
            updateView();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateToolbarTitle(String symbol) {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(coin.getFullName());
            bar.setSubtitle(symbol);
        }
    }

    private void switchCurrencyMenuTitle(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_currency);
        if (item == null) {
            return;
        }

        String symbol = PrefHelper.getToSymbol(this);

        if (symbol != null && symbol.equals(MainActivity.Currency.JPY.name())) {
            item.setTitle(MainActivity.Currency.JPY.titleStrResId);
        } else {
            item.setTitle(MainActivity.Currency.USD.titleStrResId);
        }
    }

    private void drawBoardChart() {
        new GetBoardTask(this).setListener(new GetBoardTask.Listener() {
            @Override
            public void finished(Board board) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(Tag.board.name());
                if (fragment != null) {
                    ((CoinBoardFragment) fragment).updateView(board);
                    Log.d("UPDATED", "board, " + new Date().toString());
                }
            }
        }).execute();
    }
}
