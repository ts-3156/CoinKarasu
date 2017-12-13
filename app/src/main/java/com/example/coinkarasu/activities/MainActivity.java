package com.example.coinkarasu.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import com.crashlytics.android.Crashlytics;
import com.example.coinkarasu.R;
import com.example.coinkarasu.activities.settings.SettingsActivity;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.cryptocompare.Client;
import com.example.coinkarasu.cryptocompare.ClientImpl;
import com.example.coinkarasu.cryptocompare.CoinListReader;
import com.example.coinkarasu.cryptocompare.data.CoinList;
import com.example.coinkarasu.cryptocompare.data.CoinListImpl;
import com.example.coinkarasu.utils.PrefHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        ListViewFragment.OnFragmentInteractionListener {

    private enum NavigationKind {
        nav_main(R.string.nav_main, R.id.nav_main, 0),
        jpy_toplist(R.string.nav_jpy_toplist, R.id.nav_jpy_toplist, 1),
        usd_toplist(R.string.nav_usd_toplist, R.id.nav_usd_toplist, 2),
        eur_toplist(R.string.nav_eur_toplist, R.id.nav_eur_toplist, 3),
        btc_toplist(R.string.nav_btc_toplist, R.id.nav_btc_toplist, 4);

        int titleStrResId;
        int navResId;
        int navPos;

        NavigationKind(int titleStrResId, int navResId, int navPos) {
            this.titleStrResId = titleStrResId;
            this.navResId = navResId;
            this.navPos = navPos;
        }

        static NavigationKind valueByNavResId(int navResId) {
            for (NavigationKind kind : values()) {
                if (kind.navResId == navResId) {
                    return kind;
                }
            }
            return null;
        }
    }

    private static final String FRAGMENT_TAG = "fragment";

    private ArrayList<NavigationKind> kindHistories = new ArrayList<>();
    private CoinList coinList;
    private Client client;
    private NavigationKind navigationKind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setNavChecked(NavigationKind.nav_main);

        coinList = null;
        try {
            long start = System.currentTimeMillis();
            coinList = CoinListImpl.buildByResponse(
                    new JSONObject(CoinListReader.read(this)));
            Log.d("LOAD", (System.currentTimeMillis() - start) + " ms");
        } catch (JSONException e) {
            Log.e("CLReader", e.getMessage());
        }

        client = new ClientImpl(this);
        navigationKind = NavigationKind.nav_main;
        refreshView(navigationKind);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (navigationKind != null) {
            applyKeepScreenOn();
            applyIsAnimEnabled();
            setNavChecked(navigationKind); // Return from SettingsActivity
        }
    }

    private void refreshView(NavigationKind kind) {
        updateToolbarTitle(kind);
        applyKeepScreenOn();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, ListViewFragment.newInstance(kind.name()), FRAGMENT_TAG);
        if (!kindHistories.isEmpty()) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    public Client getClient() {
        return client;
    }

    public ArrayList<Coin> collectCoins(String[] fromSymbols, String toSymbol) {
        if (coinList == null) {
            Log.e("collectCoins", "coinList is null");
            return new ArrayList<>();
        }
        ArrayList<Coin> coins = coinList.collectCoins(fromSymbols);
        for (Coin coin : coins) {
            coin.setToSymbol(toSymbol);
        }
        return coins;
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

    private void applyIsAnimEnabled() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        boolean value = PrefHelper.isAnimEnabled(this);
        if (fragment != null) {
            ((ListViewFragment) fragment).applyIsAnimEnabled(value);
        }

        Log.d("EnableAnim", "" + value);
    }

    private void startAutoUpdate() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment != null) {
            ((ListViewFragment) fragment).updateView();
        }
    }

    private void stopAutoUpdate() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment != null) {
            ((ListViewFragment) fragment).stopAutoUpdate();
        }
    }

    private void setNavChecked(NavigationKind kind) {
        ((NavigationView) findViewById(R.id.nav_view)).getMenu().getItem(kind.navPos).setChecked(true);
    }

    private void updateToolbarTitle(NavigationKind kind) {
        ActionBar bar = getSupportActionBar();
        if (bar == null) {
            return;
        }

        bar.setTitle(getResources().getString(kind.titleStrResId));
    }

    private void updateCurrencyMenuTitle(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_currency);
        if (item == null) {
            return;
        }

        if (navigationKind == NavigationKind.nav_main) {
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!kindHistories.isEmpty()) {
            navigationKind = kindHistories.get(kindHistories.size() - 1);
            kindHistories.remove(kindHistories.size() - 1);
            updateToolbarTitle(navigationKind);
            setNavChecked(navigationKind);
            super.onBackPressed();
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
        } else if (id == R.id.action_pause) {
            item.setChecked(!item.isChecked());
            if (item.isChecked()) {
                stopAutoUpdate();
            } else {
                startAutoUpdate();
            }

            return true;
        } else if (id == R.id.action_currency) {
            PrefHelper.setToSymbol(this, item.getTitle().toString());
            refreshView(navigationKind);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        NavigationKind clickedKind = NavigationKind.valueByNavResId(id);

        if (clickedKind != null && clickedKind != navigationKind) {
            kindHistories.add(navigationKind);
            navigationKind = clickedKind;
            refreshView(navigationKind);
        } else if (id == R.id.nav_settings) {
            stopAutoUpdate();
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
