package com.coinkarasu.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.view.Window;
import android.view.WindowManager;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.R;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.activities.settings.PreferencesActivity;
import com.coinkarasu.billingmodule.BillingActivity;
import com.coinkarasu.services.UpdateToplistIntentService;
import com.coinkarasu.services.UpdateTrendingIntentService;
import com.coinkarasu.utils.PrefHelper;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.MobileAds;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        MainFragment.OnFragmentInteractionListener {

    private static final String STATE_SELECTED_KIND = "kind";
    public static final NavigationKind DEFAULT_KIND = NavigationKind.home;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MobileAds.initialize(this, BuildConfig.ADMOB_APP_ID);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), BillingActivity.class);
                startActivity(intent);
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState != null) {
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, MainFragment.newInstance(DEFAULT_KIND), FRAGMENT_TAG)
                    .commit();
        }

        for (NavigationKind kind : NavigationKind.toplistValues()) {
            if (kind.isVisible(this)) {
                UpdateToplistIntentService.start(this, kind);
            }
        }
        UpdateTrendingIntentService.start(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        applyKeepScreenOn();
    }

    private void applyKeepScreenOn() {
        boolean value = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("pref_keep_screen_on", getResources().getBoolean(R.bool.keep_screen_on));

        if (value) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        Log.d("KeepScrOn", "" + value);
    }

    private void setNavChecked(NavigationKind kind) {
        NavigationView view = findViewById(R.id.nav_view);
        Menu menu = view.getMenu();

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);

            for (NavigationKind k : NavigationKind.values()) {
                if (item.getItemId() == k.navResId) {
                    item.setVisible(k.isVisible(this));

                    if (k == kind) {
                        item.setChecked(true);
                        ColorStateList list = getResources().getColorStateList(k.colorStateResId);
                        view.setItemTextColor(list);
                        view.setItemIconTintList(list);
                    }

                    break;
                }
            }
        }
    }

    private void updateToolbarTitle(NavigationKind kind) {
        ActionBar bar = getSupportActionBar();
        if (bar == null) {
            return;
        }

        bar.setTitle(kind.headerStrResId);

//        NavigationKind currentKind = getCurrentKind();
//        if (currentKind != null && currentKind == NavigationKind.japan) {
//            bar.setSubtitle(Currency.JPY.disabledTitleStrResId);
//        } else {
//            bar.setSubtitle(null);
//        }
    }

    private void updateTabColor(NavigationKind kind) {
        findViewById(R.id.tab_layout).setBackgroundColor(getResources().getColor(kind.colorResId));
        findViewById(R.id.toolbar).setBackgroundColor(getResources().getColor(kind.colorResId));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, kind.colorDarkResId));
        }
    }

    private void switchCurrencyMenuTitle(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_currency);
        if (item == null) {
            return;
        }

        NavigationKind kind = getCurrentKind();
        if (kind != null && kind == NavigationKind.japan) {
            item.setEnabled(false);
            item.setTitle(Currency.JPY.disabledTitleStrResId);
        } else {
            item.setEnabled(true);
            String symbol = PrefHelper.getToSymbol(this);

            if (symbol != null && symbol.equals(Currency.JPY.name())) {
                item.setTitle(Currency.JPY.titleStrResId);
            } else {
                item.setTitle(Currency.USD.titleStrResId);
            }
        }
    }

    private NavigationKind getCurrentKind() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            return null;
        }
        return ((MainFragment) fragment).getCurrentKind();
    }

    private void setCurrentKind(NavigationKind kind) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            return;
        }
        ((MainFragment) fragment).setCurrentKind(kind);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            NavigationKind kind = getCurrentKind();
            if (kind != null && kind != DEFAULT_KIND) {
                setCurrentKind(DEFAULT_KIND);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
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
            startActivity(new Intent(this, PreferencesActivity.class));

            return true;
        } else if (id == R.id.action_pause) {
            item.setChecked(!item.isChecked());
            if (item.isChecked()) {
                // TODO Use global variable
            } else {
            }

            return true;
        } else if (id == R.id.action_currency) {
            if (item.getTitle().toString().equals(getString(Currency.USD.titleStrResId))) {
                PrefHelper.saveToSymbol(this, Currency.JPY.name());
            } else {
                PrefHelper.saveToSymbol(this, Currency.USD.name());
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        NavigationKind clickedKind = NavigationKind.valueByNavResId(id);

        NavigationKind kind = getCurrentKind();
        if (clickedKind != null && kind != null && clickedKind != kind) {
            setCurrentKind(clickedKind);
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, PreferencesActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_SELECTED_KIND, "Saved");
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onPageChanged(NavigationKind kind) {
        setNavChecked(kind);
        updateToolbarTitle(kind);
        updateTabColor(kind);
        updateTabIconAlpha(kind);
    }

    private void updateTabIconAlpha(NavigationKind kind) {
        TabLayout tabs = findViewById(R.id.tab_layout);
        TabLayout.Tab tab = tabs.getTabAt(NavigationKind.edit_tabs.ordinal());
        if (tab == null) {
            return;
        }

        Drawable icon = tab.getIcon();
        if (icon == null) {
            return;
        }

        if (kind == NavigationKind.edit_tabs) {
            icon.setAlpha(255);
        } else {
            icon.setAlpha(204); // 80%
        }
    }
}
