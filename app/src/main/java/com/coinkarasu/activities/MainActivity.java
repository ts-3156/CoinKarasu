package com.coinkarasu.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.R;
import com.coinkarasu.activities.etc.Currency;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.activities.settings.PreferencesActivity;
import com.coinkarasu.billingmodule.BillingActivity;
import com.coinkarasu.billingmodule.billing.BillingManager;
import com.coinkarasu.tasks.GetApiKeyTask;
import com.coinkarasu.utils.ApiKeyUtils;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.PrefHelper;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        MainFragment.OnFragmentInteractionListener {

    private static final boolean DEBUG = true;
    private static final String TAG = "MainActivity";
    private static final String STATE_SELECTED_KIND = "kind";
    public static final NavigationKind DEFAULT_KIND = NavigationKind.home;
    private static final String FRAGMENT_TAG = "fragment";

    private FirebaseAnalytics firebaseAnalytics;
    private MainViewController viewController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // ここで 180ms, onCreate全体で 230ms
        CKLog.setContext(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BillingActivity.start(view.getContext(), -1);
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
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

        viewController = new MainViewController(this);
        new BillingManager(this, viewController.getUpdateListener());

        new initializeThirdPartyAppsTask(this, new Runnable() {
            @Override
            public void run() {
                setupAdView();
            }
        }).execute();
        if (!ApiKeyUtils.exists(this)) {
            new GetApiKeyTask(this).execute();
        }
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

        if (DEBUG) CKLog.d("KeepScrOn", "" + value);
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

    private void setCurrentKind(NavigationKind kind, boolean smoothScroll) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            return;
        }
        ((MainFragment) fragment).setCurrentKind(kind, smoothScroll);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            NavigationKind kind = getCurrentKind();
            if (kind != null && kind != DEFAULT_KIND) {
                setCurrentKind(DEFAULT_KIND, true);
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
            setCurrentKind(clickedKind, false);
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

    private void setupAdView() {
        if (PrefHelper.isPremium(this)) {
            return;
        }

        final AdView ad = new AdView(this);
        ad.setAdSize(AdSize.SMART_BANNER);
        ad.setAdUnitId(BuildConfig.ADMOB_UNIT_ID);

        ad.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (ad.getParent() == null) {
                    ViewGroup parent = findViewById(R.id.main_ad_container);
                    parent.addView(ad);

                    int adHeight = AdSize.SMART_BANNER.getHeightInPixels(MainActivity.this);
                    findViewById(R.id.fragment_container).setPadding(0, 0, 0, adHeight);

                    if (DEBUG) CKLog.d("onAdLoaded", "loaded");
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                if (DEBUG) CKLog.e(TAG, "onAdFailedToLoad() " + errorCode);
            }
        });
        ad.loadAd(new AdRequest.Builder().build());
    }

    public boolean isPremiumPurchased() {
        return viewController.isPremiumPurchased();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CKLog.releaseContext();
    }

    public void setFirebaseAnalytics(FirebaseAnalytics firebaseAnalytics) {
        this.firebaseAnalytics = firebaseAnalytics;
    }

    private static class initializeThirdPartyAppsTask extends AsyncTask<Void, Void, Void> {
        private MainActivity activity;
        private Runnable runnable;

        initializeThirdPartyAppsTask(MainActivity activity, Runnable runnable) {
            this.activity = activity;
            this.runnable = runnable;
        }

        @Override
        protected Void doInBackground(Void... params) {
            long start = System.currentTimeMillis();
            Fabric.with(activity, new Crashlytics());
            activity.setFirebaseAnalytics(FirebaseAnalytics.getInstance(activity));
            MobileAds.initialize(activity, BuildConfig.ADMOB_APP_ID);
            if (DEBUG) CKLog.d(TAG, "initializeThirdPartyAppsTask() " + (System.currentTimeMillis() - start));

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (runnable != null) {
                runnable.run();
            }
            activity = null;
            runnable = null;
        }
    }
}
