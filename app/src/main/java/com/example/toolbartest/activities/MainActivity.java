package com.example.toolbartest.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.example.toolbartest.R;
import com.example.toolbartest.activities.settings.SettingsActivity;
import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.coins.SectionHeaderCoinImpl;
import com.example.toolbartest.cryptocompare.Client;
import com.example.toolbartest.cryptocompare.ClientImpl;
import com.example.toolbartest.cryptocompare.data.Prices;
import com.example.toolbartest.tasks.GetPricesTask;
import com.example.toolbartest.utils.AnimHelper;
import com.example.toolbartest.utils.AutoUpdateTimer;
import com.example.toolbartest.utils.Exchange;
import com.example.toolbartest.utils.ExchangeImpl;
import com.example.toolbartest.utils.PrefHelper;
import com.example.toolbartest.utils.ResNameHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ListWithHeaderFragment.OnFragmentInteractionListener, GetPricesTask.Listener {

    private static final String FRAGMENT_TAG = "fragment";

    private ArrayList<Coin> coins;
    private Client client;
    private ArrayList<String> exchanges = new ArrayList<>();

    private AutoUpdateTimer autoUpdateTimer;

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
        navigationView.getMenu().getItem(0).setChecked(true);

        client = new ClientImpl(this);
        refreshCoinListView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (autoUpdateTimer == null) {
            applyKeepScreenOn();
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

    public ArrayList<Coin> groupedCoins(String[] exchanges) {
        ArrayList<Coin> sectionalCoins = new ArrayList<>();

        if (exchanges.length == 1) {
            sectionalCoins.add(new SectionHeaderCoinImpl(ExchangeImpl.exchangeToDisplayName(exchanges[0]), exchanges[0]));
            sectionalCoins.addAll(coins);
            return sectionalCoins;
        }

        for (String exchange : exchanges) {
            sectionalCoins.add(new SectionHeaderCoinImpl(ExchangeImpl.exchangeToDisplayName(exchange), exchange));

            switch (exchange) {
                case "bitflyer":
                    sectionalCoins.addAll(coins.subList(0, 1));
                    break;
                case "coincheck":
                    sectionalCoins.addAll(coins.subList(1, 2));
                    break;
                case "zaif":
                    sectionalCoins.addAll(coins.subList(2, coins.size()));
                    break;
                default:
                    throw new RuntimeException("Invalid exchange " + exchange);
            }
        }

        return sectionalCoins;
    }

    private void replaceFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment != null) {
            transaction.remove(fragment);
        }

        exchanges.clear();

        if (ResNameHelper.useFixedListView(this)) {
            exchanges.add("bitflyer");
            exchanges.add("coincheck");
            exchanges.add("zaif");
        } else {
            exchanges.add("cccagg");
        }

        transaction.replace(R.id.fragment_container,
                ListWithHeaderFragment.newInstance(exchanges.toArray(new String[exchanges.size()])), FRAGMENT_TAG);

        transaction.commit();
    }

    private void refreshCoinListView() {
        stopAutoUpdate();
        updateToolbarTitle();

        String toSymbol = ResNameHelper.useFixedListView(this) ? "JPY" : PrefHelper.getToSymbol(this);
        coins = client.collectCoins(ResNameHelper.getFromSymbols(this), toSymbol);

        replaceFragment();
        applyKeepScreenOn();
        startAutoUpdate(0);
    }

    private void applyKeepScreenOn() {
        boolean value = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("pref_keep_screen_on", false);

        if (value) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        Log.d("KeepScrOn", "" + value);
    }

    private void startAutoUpdate(int delay) {
        if (autoUpdateTimer != null) {
            stopAutoUpdate();
        }

        autoUpdateTimer = new AutoUpdateTimer(PrefHelper.getToSymbol(this));

        autoUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (ResNameHelper.useFixedListView(MainActivity.this)) {
                    for (String exchange : exchanges) {
                        Exchange ex = new ExchangeImpl(exchange);
                        new GetPricesTask(client)
                                .setFromSymbols(ex.getFromSymbols())
                                .setToSymbol("JPY")
                                .setExchange(exchange)
                                .setListener(MainActivity.this).execute();
                    }
                } else {
                    new GetPricesTask(client)
                            .setFromSymbols(ResNameHelper.getFromSymbols(MainActivity.this))
                            .setToSymbol(PrefHelper.getToSymbol(MainActivity.this))
                            .setExchange("cccagg")
                            .setListener(MainActivity.this).execute();
                }
            }
        }, delay, PrefHelper.getSyncInterval(this));
    }

    public void stopAutoUpdate() {
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
            autoUpdateTimer = null;
        }
    }


    @Override
    public void started(String exchange, String[] fromSymbols, String toSymbol) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);

        if (fragment != null) {
            ((ListWithHeaderFragment) fragment).setProgressbarVisibility(true, exchange);
        }
    }

    @Override
    public void finished(Prices prices) {
        String toSymbol = PrefHelper.getToSymbol(this);
        if (autoUpdateTimer == null || !autoUpdateTimer.getTag().equals(toSymbol)) {
            return;
        }

        String exchange = prices.getExchange();
        ArrayList<Coin> filteredCoins = groupedCoins(new String[]{exchange});
        prices.setAttrsToCoins(filteredCoins);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);

        if (fragment != null) {
            ((ListWithHeaderFragment) fragment).updateView();
            ((ListWithHeaderFragment) fragment).setProgressbarVisibilityDelayed(false, exchange);
            Log.d("UPDATED", exchange + ", " + new Date().toString());
        }
    }

    public void updateToolbarTitle() {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(ResNameHelper.getToolbarTitle(this));
        }
    }

    private void updateCurrencyMenuTitle(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_currency);
        if (item == null) {
            return;
        }

        if (ResNameHelper.useFixedListView(this)) {
            item.setVisible(false);
        } else {
            item.setVisible(true);

            if (PrefHelper.getToSymbol(this).equals("JPY")) {
                item.setTitle("USD");
            } else {
                item.setTitle("JPY");
            }
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateCurrencyMenuTitle(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            stopAutoUpdate();
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
        } else if (id == R.id.action_pause) {
            item.setChecked(!item.isChecked());
            if (item.isChecked()) {
                stopAutoUpdate();
            } else {
                startAutoUpdate(0);
            }

            return true;
        } else if (id == R.id.action_currency) {
            PrefHelper.setToSymbol(this, item.getTitle().toString());
            refreshCoinListView();

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
        } else if (id == R.id.nav_settings) {
            stopAutoUpdate();
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
