package com.example.toolbartest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.toolbartest.R;
import com.example.toolbartest.adapters.CoinArrayAdapter;
import com.example.toolbartest.adapters.CustomAdapter;
import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.cryptocompare.Client;
import com.example.toolbartest.cryptocompare.ClientImpl;
import com.example.toolbartest.cryptocompare.data.Prices;
import com.example.toolbartest.tasks.GetPricesTask;
import com.example.toolbartest.utils.ResNameHelper;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String COIN_ACTIVITY_COIN_NAME_KEY = "COIN_NAME_KEY";
    public static final String COIN_ACTIVITY_COIN_SYMBOL_KEY = "COIN_SYMBOL_KEY";

    private static final int AUTO_UPDATE_INTERVAL = 5000;

    private ArrayList<Coin> displayCoins;
    CoinArrayAdapter coinArrayAdapter;
    Client client;

    private Timer autoUpdateTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        displayCoins = new ArrayList<>();
        coinArrayAdapter = null;

        client = new ClientImpl(this);

        initializeCoinListView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        autoUpdateCoinListPrices(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        autoUpdateCoinListPrices(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelAutoUpdateCoinListPrices();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelAutoUpdateCoinListPrices();
    }

    private void initializeCoinListView() {
        displayCoins = client.collectCoins(ResNameHelper.getFromSymbols(this), ResNameHelper.getToSymbol());
        coinArrayAdapter = new CoinArrayAdapter(this, displayCoins);
        getSupportActionBar().setTitle(ResNameHelper.getToolbarTitle(this));

        ListView listView = findViewById(R.id.coin_list);
        listView.setAdapter(coinArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                Coin coin = (Coin) ((ListView) parent).getItemAtPosition(pos);

                Intent intent = new Intent(view.getContext(), CoinActivity.class);
                intent.putExtra(COIN_ACTIVITY_COIN_NAME_KEY, coin.getCoinName());
                intent.putExtra(COIN_ACTIVITY_COIN_SYMBOL_KEY, coin.getSymbol());
                startActivity(intent);
            }
        });

        autoUpdateCoinListPrices(0);
    }

    private void refreshCoinListView() {
        cancelAutoUpdateCoinListPrices();

        getSupportActionBar().setTitle(ResNameHelper.getToolbarTitle(this));
        displayCoins = client.collectCoins(ResNameHelper.getFromSymbols(this), ResNameHelper.getToSymbol());
        autoUpdateCoinListPrices(0);
    }

    private void autoUpdateCoinListPrices(int delay) {
        if (autoUpdateTimer != null) {
            return;
        }
        autoUpdateTimer = new Timer();

        autoUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                new GetPricesTask(client)
                        .setFromSymbols(ResNameHelper.getFromSymbols(MainActivity.this))
                        .setToSymbol(ResNameHelper.getToSymbol())
                        .setExchange(ResNameHelper.getExchangeName(MainActivity.this))
                        .setListener(new GetPricesTask.Listener() {
                            @Override
                            public void finished(Prices prices) {
                                prices.setPriceAndTrendToCoins(displayCoins);
                                coinArrayAdapter.setCoins(displayCoins);
                                coinArrayAdapter.notifyDataSetChanged();
                            }
                        }).execute();
            }
        }, delay, AUTO_UPDATE_INTERVAL);
    }

    private void cancelAutoUpdateCoinListPrices() {
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
            autoUpdateTimer = null;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        String symbolsName = ResNameHelper.getSymbolsName(this);

        if (id == R.id.nav_main) {
            if (!symbolsName.equals(ResNameHelper.SYMBOLS_NAME_MAIN)) {
                ResNameHelper.setSymbolsName(this, ResNameHelper.SYMBOLS_NAME_MAIN);
                refreshCoinListView();
            }
        } else if (id == R.id.nav_jpy_toplist) {
            if (!symbolsName.equals(ResNameHelper.SYMBOLS_NAME_JPY_TOPLIST)) {
                ResNameHelper.setSymbolsName(this, ResNameHelper.SYMBOLS_NAME_JPY_TOPLIST);
                refreshCoinListView();
            }
        } else if (id == R.id.nav_usd_toplist) {
            if (!symbolsName.equals(ResNameHelper.SYMBOLS_NAME_USD_TOPLIST)) {
                ResNameHelper.setSymbolsName(this, ResNameHelper.SYMBOLS_NAME_USD_TOPLIST);
                refreshCoinListView();
            }
        } else if (id == R.id.nav_japan_all) {
            if (!symbolsName.equals(ResNameHelper.SYMBOLS_NAME_JAPAN_ALL)) {
                ResNameHelper.setSymbolsName(this, ResNameHelper.SYMBOLS_NAME_JAPAN_ALL);
                refreshCoinListView();
            }
        } else if (id == R.id.nav_bitflyer) {
            if (!symbolsName.equals(ResNameHelper.SYMBOLS_NAME_BITFLYER)) {
                ResNameHelper.setSymbolsName(this, ResNameHelper.SYMBOLS_NAME_BITFLYER);
                refreshCoinListView();
            }
        } else if (id == R.id.nav_coincheck) {
            if (!symbolsName.equals(ResNameHelper.SYMBOLS_NAME_COINCHECK)) {
                ResNameHelper.setSymbolsName(this, ResNameHelper.SYMBOLS_NAME_COINCHECK);
                refreshCoinListView();
            }
        } else if (id == R.id.nav_zaif) {
            if (!symbolsName.equals(ResNameHelper.SYMBOLS_NAME_ZAIF)) {
                ResNameHelper.setSymbolsName(this, ResNameHelper.SYMBOLS_NAME_ZAIF);
                refreshCoinListView();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
