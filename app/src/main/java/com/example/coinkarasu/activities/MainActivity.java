package com.example.coinkarasu.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
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
import com.example.coinkarasu.adapters.ViewPagerAdapter;
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
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        ListViewFragment.OnFragmentInteractionListener,
        ViewPager.OnPageChangeListener {

    public static final NavigationKind DEFAULT_KIND = NavigationKind.nav_main;

    public enum NavigationKind {
        nav_main(R.string.nav_main, R.string.tab_main, R.id.nav_main, 0),
        jpy_toplist(R.string.nav_jpy_toplist, R.string.tab_jpy_toplist, R.id.nav_jpy_toplist, 2),
        usd_toplist(R.string.nav_usd_toplist, R.string.tab_usd_toplist, R.id.nav_usd_toplist, 3),
        eur_toplist(R.string.nav_eur_toplist, R.string.tab_eur_toplist, R.id.nav_eur_toplist, 4),
        btc_toplist(R.string.nav_btc_toplist, R.string.tab_btc_toplist, R.id.nav_btc_toplist, 5);

        int navStrResId;
        int tabStrResId;
        int navResId;
        int navPos;

        NavigationKind(int navStrResId, int tabStrResId, int navResId, int navPos) {
            this.navStrResId = navStrResId;
            this.tabStrResId = tabStrResId;
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

    public enum Currency {
        JPY(R.string.action_currency_switch_to_usd, R.string.action_currency_only_for_jpy),
        USD(R.string.action_currency_switch_to_jpy, -1);

        int titleStrResId;
        int disabledTitleStrResId;

        Currency(int titleStrResId, int disabledTitleStrResId) {
            this.titleStrResId = titleStrResId;
            this.disabledTitleStrResId = disabledTitleStrResId;
        }
    }

    private static final String FRAGMENT_TAG = "fragment";

    private CoinList coinList;
    private Client client;
    private NavigationKind navigationKind;

    private ViewPager pager;
    private TabLayout tabs;
    private TabLayout.Tab tab;

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

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addItem(ListViewFragment.newInstance(NavigationKind.nav_main));
        adapter.addItem(ListViewFragment.newInstance(NavigationKind.jpy_toplist));
        adapter.addItem(ListViewFragment.newInstance(NavigationKind.usd_toplist));
        adapter.addItem(ListViewFragment.newInstance(NavigationKind.eur_toplist));
        adapter.addItem(ListViewFragment.newInstance(NavigationKind.btc_toplist));

        pager = findViewById(R.id.view_pager);
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(this);
        pager.setOffscreenPageLimit(NavigationKind.values().length);

        tabs = findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(pager);

        tabs.getTabAt(NavigationKind.nav_main.ordinal()).setText(getResources().getString(NavigationKind.nav_main.tabStrResId));
        tabs.getTabAt(NavigationKind.jpy_toplist.ordinal()).setText(getResources().getString(NavigationKind.jpy_toplist.tabStrResId));
        tabs.getTabAt(NavigationKind.usd_toplist.ordinal()).setText(getResources().getString(NavigationKind.usd_toplist.tabStrResId));
        tabs.getTabAt(NavigationKind.eur_toplist.ordinal()).setText(getResources().getString(NavigationKind.eur_toplist.tabStrResId));
        tabs.getTabAt(NavigationKind.btc_toplist.ordinal()).setText(getResources().getString(NavigationKind.btc_toplist.tabStrResId));

        tab = tabs.getTabAt(NavigationKind.nav_main.ordinal());

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
        navigationKind = DEFAULT_KIND;
        pager.setCurrentItem(navigationKind.ordinal());
        pageChanged(navigationKind.ordinal()); // repeated in onResume
    }

    @Override
    public void onResume() {
        super.onResume();

        if (navigationKind != null) {
            applyKeepScreenOn();
            pageChanged(navigationKind.ordinal()); // Return from SettingsActivity
        }
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

    private void startAutoUpdate(int position) {
        ViewPagerAdapter adapter = (ViewPagerAdapter) pager.getAdapter();
        ((ListViewFragment) adapter.getItem(position)).updateView();
    }

    private void stopAutoUpdate() {
        ViewPagerAdapter adapter = (ViewPagerAdapter) pager.getAdapter();
        List<Fragment> fragments = adapter.getItems();
        for (int i = 0; i < fragments.size(); i++) {
            ((ListViewFragment) fragments.get(i)).stopAutoUpdate();
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

        bar.setTitle(getResources().getString(kind.navStrResId));
    }

    private void switchCurrencyMenuTitle(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_currency);
        if (item == null) {
            return;
        }

        if (navigationKind == NavigationKind.nav_main) {
            item.setEnabled(false);
            item.setTitle(getResources().getString(Currency.JPY.disabledTitleStrResId));
        } else {
            item.setEnabled(true);
            String symbol = PrefHelper.getToSymbol(this);

            if (symbol != null && symbol.equals(Currency.JPY.name())) {
                item.setTitle(getResources().getString(Currency.JPY.titleStrResId));
            } else {
                item.setTitle(getResources().getString(Currency.USD.titleStrResId));
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (navigationKind != NavigationKind.nav_main) {
            pager.setCurrentItem(NavigationKind.nav_main.ordinal());
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
        switchCurrencyMenuTitle(menu);
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
                if (tab != null) {
                    startAutoUpdate(tab.getPosition());
                }
            }

            return true;
        } else if (id == R.id.action_currency) {
            if (item.getTitle().toString().equals(getResources().getString(Currency.USD.titleStrResId))) {
                PrefHelper.setToSymbol(this, Currency.JPY.name());
            } else {
                PrefHelper.setToSymbol(this, Currency.USD.name());
            }
            pageChanged(navigationKind.ordinal());

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        NavigationKind clickedKind = NavigationKind.valueByNavResId(id);

        if (clickedKind != null && clickedKind != navigationKind) {
            pager.setCurrentItem(clickedKind.ordinal());
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
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_SETTLING) {
            int position = pager.getCurrentItem();

            if (position != tab.getPosition()) {
                pageChanged(position);
            }
        }
    }

    private void pageChanged(int position) {
        stopAutoUpdate();
        startAutoUpdate(position);

        navigationKind = NavigationKind.values()[position];
        tab = tabs.getTabAt(navigationKind.ordinal());
        setNavChecked(navigationKind);
        updateToolbarTitle(navigationKind);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
