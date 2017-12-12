package com.example.coinkarasu.activities;

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
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.example.coinkarasu.activities.settings.SettingsActivity;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.cryptocompare.Client;
import com.example.coinkarasu.cryptocompare.ClientImpl;
import com.example.coinkarasu.utils.PrefHelper;
import com.example.coinkarasu.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ListWithHeaderFragment.OnFragmentInteractionListener {

    private enum NavigationKind {
        japan_all(R.string.nav_japan_all),
        jpy_toplist(R.string.nav_jpy_toplist),
        usd_toplist(R.string.nav_usd_toplist);

        int symbolsResId;

        NavigationKind(int symbolsResId) {
            this.symbolsResId = symbolsResId;
        }
    }

    private static final String FRAGMENT_TAG = "fragment";

    private Client client;
    private NavigationKind navigationKind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        navigationView.getMenu().getItem(0).setChecked(true);

        navigationKind = NavigationKind.japan_all;
        client = new ClientImpl(this);
        refreshView(navigationKind);
    }

    private void replaceFragment(NavigationKind kind) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, ListWithHeaderFragment.newInstance(kind.name()), FRAGMENT_TAG)
                .commit();
    }

    private void refreshView(NavigationKind kind) {
        updateToolbarTitle(kind);
        replaceFragment(kind);
        applyKeepScreenOn();
    }

    public Client getClient() {
        return client;
    }

    public ArrayList<Coin> collectCoins(String[] fromSymbols, String toSymbol) {
        ArrayList<Coin> coins = client.collectCoins(fromSymbols);
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

    private void startAutoUpdate() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment != null) {
            ((ListWithHeaderFragment) fragment).updateView();
        }
    }

    private void stopAutoUpdate() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment != null) {
            ((ListWithHeaderFragment) fragment).stopAutoUpdate();
        }
    }

    private void updateToolbarTitle(NavigationKind kind) {
        ActionBar bar = getSupportActionBar();
        if (bar == null) {
            return;
        }

        bar.setTitle(getResources().getString(kind.symbolsResId));
    }

    private void updateCurrencyMenuTitle(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_currency);
        if (item == null) {
            return;
        }

        if (navigationKind == NavigationKind.japan_all) {
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_main && navigationKind != NavigationKind.japan_all) {
            navigationKind = NavigationKind.japan_all;
            refreshView(navigationKind);
        } else if (id == R.id.nav_jpy_toplist && navigationKind != NavigationKind.jpy_toplist) {
            navigationKind = NavigationKind.jpy_toplist;
            refreshView(navigationKind);
        } else if (id == R.id.nav_usd_toplist && navigationKind != NavigationKind.usd_toplist) {
            navigationKind = NavigationKind.usd_toplist;
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
