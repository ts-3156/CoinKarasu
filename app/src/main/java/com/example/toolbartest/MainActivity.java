package com.example.toolbartest;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.coins.CoinList;
import com.example.toolbartest.cryptocompare.Client;
import com.example.toolbartest.cryptocompare.ClientImpl;
import com.example.toolbartest.utils.ResourceHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String MAIN_ACTIVITY_COIN_IDS_RESOURCE_NAME = "COIN_IDS_RESOURCE_NAME";
    public static final String COIN_ACTIVITY_COIN_NAME_KEY = "COIN_NAME_KEY";
    public static final String COIN_ACTIVITY_COIN_SYMBOL_KEY = "COIN_SYMBOL_KEY";

    private static final String DEFAULT_COIN_SYMBOLS_RESOURCE_NAME = "default_watch_list_symbols";

    private CoinList coinList;
    CoinArrayAdapter coinArrayAdapter;
    Client client;

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

        coinList = null;
        coinArrayAdapter = null;

        client = new ClientImpl(this);
        client.getCoinList(new ClientImpl.CoinListListener() {
            @Override
            public void finished(CoinList coinList) {
                MainActivity.this.coinList = coinList;
                initializeCoinListView();
            }
        });
    }

    private String getCurrentCoinSymbolsResourceName() {
        String name = getIntent().getStringExtra(MainActivity.MAIN_ACTIVITY_COIN_IDS_RESOURCE_NAME);
        if (name == null) {
            name = DEFAULT_COIN_SYMBOLS_RESOURCE_NAME;
        }
        return name;
    }

    private void setCurrentCoinSymbolsResourceName(String name) {
        getIntent().putExtra(MainActivity.MAIN_ACTIVITY_COIN_IDS_RESOURCE_NAME, name);
    }

    private ArrayList<Coin> collectCoins(final CollectCoinsListener listener) {
        final ArrayList<Coin> coins = new ArrayList<>();
        final String[] coinSymbols = ResourceHelper.getStringArrayResourceByName(this, getCurrentCoinSymbolsResourceName());

        client.getCoinPrices(coinSymbols, "JPY", new ClientImpl.CoinPricesListener() {
            @Override
            public void finished(HashMap<String, Double> prices) {
                for (String coinSymbol : coinSymbols) {
                    Coin coin = coinList.getCoinBySymbol(coinSymbol);
                    if (coin == null) {
                        continue;
                    }

                    Double price = prices.get(coinSymbol);
                    if (price != null) {
                        coin.setPrice(price);
                    }

                    coins.add(coin);
                }

                listener.finished(coins);
            }
        });

        return coins;
    }

    private interface CollectCoinsListener {
        void finished(ArrayList<Coin> coins);
    }

    private void initializeCoinListView() {
        collectCoins(new CollectCoinsListener() {
            @Override
            public void finished(ArrayList<Coin> coins) {
                ListView listView = findViewById(R.id.coin_list);
                coinArrayAdapter = new CoinArrayAdapter(MainActivity.this, coins);
                listView.setAdapter(coinArrayAdapter);
                updateTitle();

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                        Coin coin = (Coin) ((ListView) parent).getItemAtPosition(pos);

                        Intent intent = new Intent(view.getContext(), CoinActivity.class);
                        intent.putExtra(COIN_ACTIVITY_COIN_NAME_KEY, coin.getCoinName());
                        intent.putExtra(COIN_ACTIVITY_COIN_SYMBOL_KEY, coin.getSymbol());
                        startActivity(intent);
                    }
                });

            }
        });
    }

    private void updateCoinListView() {
        collectCoins(new CollectCoinsListener() {
            @Override
            public void finished(ArrayList<Coin> coins) {
                coinArrayAdapter.setCoins(coins);
                coinArrayAdapter.notifyDataSetChanged();
                updateTitle();
            }
        });
    }

    private void updateTitle() {
        String title = "";

        switch (getCurrentCoinSymbolsResourceName()) {
            case DEFAULT_COIN_SYMBOLS_RESOURCE_NAME:
                title = getResources().getString(R.string.default_list);
                break;
            case "jpy_toplist_symbols":
                title = getResources().getString(R.string.jpy_toplist);
                break;
            case "usd_toplist_symbols":
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
            if (!getCurrentCoinSymbolsResourceName().equals(DEFAULT_COIN_SYMBOLS_RESOURCE_NAME)) {
                setCurrentCoinSymbolsResourceName(DEFAULT_COIN_SYMBOLS_RESOURCE_NAME);
                updateCoinListView();
            }
        } else if (id == R.id.nav_gallery) {
            if (!getCurrentCoinSymbolsResourceName().equals("jpy_toplist_symbols")) {
                setCurrentCoinSymbolsResourceName("jpy_toplist_symbols");
                updateCoinListView();
            }
        } else if (id == R.id.nav_slideshow) {
            if (!getCurrentCoinSymbolsResourceName().equals("usd_toplist_symbols")) {
                setCurrentCoinSymbolsResourceName("usd_toplist_symbols");
                updateCoinListView();
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
