package com.example.toolbartest;

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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.cryptocompare.Client;
import com.example.toolbartest.cryptocompare.ClientImpl;
import com.example.toolbartest.cryptocompare.data.Prices;
import com.example.toolbartest.tasks.GetCoinsTask;
import com.example.toolbartest.tasks.GetPricesTask;
import com.example.toolbartest.utils.ResourceHelper;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String COIN_ACTIVITY_COIN_NAME_KEY = "COIN_NAME_KEY";
    public static final String COIN_ACTIVITY_COIN_SYMBOL_KEY = "COIN_SYMBOL_KEY";

    private static final String COIN_SYMBOLS_RESOURCE_NAME_KEY = "COIN_SYMBOLS_RESOURCE_NAME_KEY";
    private static final String DEFAULT_COIN_SYMBOLS_RESOURCE_NAME = "default_watch_list_symbols";
    private static final String JPY_TOPLIST_COIN_SYMBOLS_RESOURCE_NAME = "jpy_toplist_symbols";
    private static final String USD_TOPLIST_COIN_SYMBOLS_RESOURCE_NAME = "usd_toplist_symbols";
    private static final String DEFAULT_TO_SYMBOL = "JPY";
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

        new GetCoinsTask(client)
                .setFromSymbols(getCoinSymbols())
                .setToSymbol(getToSymbol())
                .setListener(new GetCoinsTask.Listener() {
                    @Override
                    public void finished(ArrayList<Coin> coins) {
                        MainActivity.this.displayCoins = coins;
                        initializeCoinListView();
                    }
                }).execute();
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

    private String getCoinSymbolsResourceName() {
        String name = getIntent().getStringExtra(MainActivity.COIN_SYMBOLS_RESOURCE_NAME_KEY);
        if (name == null) {
            name = DEFAULT_COIN_SYMBOLS_RESOURCE_NAME;
        }
        return name;
    }

    private void setCoinSymbolsResourceName(String name) {
        getIntent().putExtra(MainActivity.COIN_SYMBOLS_RESOURCE_NAME_KEY, name);
    }

    private String[] getCoinSymbols() {
        return ResourceHelper.getStringArrayResourceByName(this, getCoinSymbolsResourceName());
    }

    private String getToSymbol() {
        return DEFAULT_TO_SYMBOL;
    }

    private void initializeCoinListView() {
        displayCoins = client.collectCoins(getCoinSymbols(), getToSymbol());
        coinArrayAdapter = new CoinArrayAdapter(this, displayCoins);

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

        displayCoins = client.collectCoins(getCoinSymbols(), getToSymbol());
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
                        .setFromSymbols(getCoinSymbols())
                        .setToSymbol(getToSymbol())
                        .setListener(new GetPricesTask.Listener() {
                            @Override
                            public void finished(Prices prices) {
                                prices.setPriceAndTrendToCoins(displayCoins);
                                updateTitle();
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

    private void updateTitle() {
        String title = "";

        switch (getCoinSymbolsResourceName()) {
            case DEFAULT_COIN_SYMBOLS_RESOURCE_NAME:
                title = getResources().getString(R.string.default_list);
                break;
            case JPY_TOPLIST_COIN_SYMBOLS_RESOURCE_NAME:
                title = getResources().getString(R.string.jpy_toplist);
                break;
            case USD_TOPLIST_COIN_SYMBOLS_RESOURCE_NAME:
                title = getResources().getString(R.string.usd_toplist);
                break;
        }

        getSupportActionBar().setTitle(title);
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

        if (id == R.id.nav_camera) {
            if (!getCoinSymbolsResourceName().equals(DEFAULT_COIN_SYMBOLS_RESOURCE_NAME)) {
                setCoinSymbolsResourceName(DEFAULT_COIN_SYMBOLS_RESOURCE_NAME);
                refreshCoinListView();
            }
        } else if (id == R.id.nav_gallery) {
            if (!getCoinSymbolsResourceName().equals(JPY_TOPLIST_COIN_SYMBOLS_RESOURCE_NAME)) {
                setCoinSymbolsResourceName(JPY_TOPLIST_COIN_SYMBOLS_RESOURCE_NAME);
                refreshCoinListView();
            }
        } else if (id == R.id.nav_slideshow) {
            if (!getCoinSymbolsResourceName().equals(USD_TOPLIST_COIN_SYMBOLS_RESOURCE_NAME)) {
                setCoinSymbolsResourceName(USD_TOPLIST_COIN_SYMBOLS_RESOURCE_NAME);
                refreshCoinListView();
            }
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
