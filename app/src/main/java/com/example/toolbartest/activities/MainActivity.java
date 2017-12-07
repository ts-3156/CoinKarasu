package com.example.toolbartest.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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
import com.example.toolbartest.tasks.GetPricesOverJapaneseExchangesTask;
import com.example.toolbartest.tasks.GetPricesTask;
import com.example.toolbartest.utils.ResNameHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MainFragment.OnFragmentInteractionListener, FixedMainFragment.OnFragmentInteractionListener {

    public static final String COIN_ACTIVITY_COIN_NAME_KEY = "COIN_NAME_KEY";
    public static final String COIN_ACTIVITY_COIN_SYMBOL_KEY = "COIN_SYMBOL_KEY";

    private ArrayList<Coin> coins;
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
        navigationView.getMenu().getItem(0).setChecked(true);

        client = new ClientImpl(this);
        initializeCoinListView();
    }

    @Override
    protected void onStart() {
        super.onStart();
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

    public ArrayList<Coin> getSectionInsertedCoins() {
        if (!ResNameHelper.getSymbolsName(this).equals(ResNameHelper.SYMBOLS_NAME_JAPAN_ALL)) {
            return coins;
        }

        ArrayList<Coin> sectionalCoins = new ArrayList<>();

        for (int i = 0; i < coins.size(); i++) {
            if (i == 0) {
                sectionalCoins.add(new SectionHeaderCoinImpl(getResources().getString(R.string.nav_bitflyer)));
            } else if (i == 1) {
                sectionalCoins.add(new SectionHeaderCoinImpl(getResources().getString(R.string.nav_coincheck)));
            } else if (i == 2) {
                sectionalCoins.add(new SectionHeaderCoinImpl(getResources().getString(R.string.nav_zaif)));
            }

            Coin coin = coins.get(i);
            sectionalCoins.add(coin);
        }

        return sectionalCoins;
    }

    private void replaceFragment() {
        Fragment fragment = null;

        if (ResNameHelper.useFixedListView(this)) {
            fragment = FixedMainFragment.newInstance();
        } else {
            fragment = MainFragment.newInstance();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, "fragment").commit();
    }

    private void initializeCoinListView() {
        getSupportActionBar().setTitle(ResNameHelper.getToolbarTitle(this));
        coins = client.collectCoins(ResNameHelper.getFromSymbols(this), ResNameHelper.getToSymbol());
        replaceFragment();
        applyKeepScreenOn();
        startAutoUpdate(0);
    }

    private void refreshCoinListView() {
        stopAutoUpdate();
        getSupportActionBar().setTitle(ResNameHelper.getToolbarTitle(this));
        coins = client.collectCoins(ResNameHelper.getFromSymbols(this), ResNameHelper.getToSymbol());
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

        String value = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getString("sync_frequency", "10000");
        int interval = Integer.valueOf(value);

        if (interval <= 0) {
            return;
        }

        if (interval < 5000) {
            Log.d("Interval", "" + interval);
            interval = 10000;
        }


        autoUpdateTimer = new Timer();

        autoUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (ResNameHelper.useFixedListView(MainActivity.this)) {
                    new GetPricesOverJapaneseExchangesTask(client)
                            .setListener(new GetPricesOverJapaneseExchangesTask.Listener() {
                                @Override
                                public void finished(HashMap<String, Prices> map) {
                                    if (autoUpdateTimer == null) {
                                        return;
                                    }
                                    map.get(GetPricesOverJapaneseExchangesTask.EXCHANGE_BITFLYER).setAttrsToCoin(coins.get(0));
                                    map.get(GetPricesOverJapaneseExchangesTask.EXCHANGE_COINCHECK).setAttrsToCoin(coins.get(1));
                                    map.get(GetPricesOverJapaneseExchangesTask.EXCHANGE_ZAIF).setAttrsToCoins(coins.subList(2, coins.size()));

                                    Fragment fragment = getSupportFragmentManager().findFragmentByTag("fragment");
                                    ((FixedMainFragment) fragment).updateCoinListView(getSectionInsertedCoins());
                                    Log.d("UPDATED", new Date().toString());
                                }
                            }).execute();

                } else {
                    new GetPricesTask(client)
                            .setFromSymbols(ResNameHelper.getFromSymbols(MainActivity.this))
                            .setToSymbol(ResNameHelper.getToSymbol())
                            .setExchange(ResNameHelper.getExchangeName(MainActivity.this))
                            .setListener(new GetPricesTask.Listener() {
                                @Override
                                public void finished(Prices prices) {
                                    if (autoUpdateTimer == null) {
                                        return;
                                    }
                                    prices.setAttrsToCoins(coins);

                                    Fragment fragment = getSupportFragmentManager().findFragmentByTag("fragment");
                                    ((MainFragment) fragment).updateCoinListView(getSectionInsertedCoins());
                                    Log.d("UPDATED", new Date().toString());
                                }
                            }).execute();
                }
            }
        }, delay, interval);
    }

    public void stopAutoUpdate() {
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
