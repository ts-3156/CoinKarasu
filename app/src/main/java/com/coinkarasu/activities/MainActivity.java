package com.coinkarasu.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.R;
import com.coinkarasu.activities.etc.Currency;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.activities.settings.PreferencesActivity;
import com.coinkarasu.billingmodule.BillingActivity;
import com.coinkarasu.billingmodule.billing.BillingManager;
import com.coinkarasu.tasks.GetApiKeyTask;
import com.coinkarasu.tasks.InitializeThirdPartyAppsTask;
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

import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        MainFragment.OnFragmentInteractionListener,
        InitializeThirdPartyAppsTask.FirebaseAnalyticsReceiver {

    private static final boolean DEBUG = true;
    private static final String TAG = "MainActivity";
    private static final String FRAGMENT_TAG = "fragment";

    private FirebaseAnalytics firebaseAnalytics;
    private MainViewController viewController;
    private MainFragment fragment;

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
            fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        } else {
            fragment = new MainFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment, FRAGMENT_TAG)
                    .commit();
        }

        viewController = new MainViewController(this);
        new BillingManager(this, viewController.getUpdateListener());

        new InitializeActivityTask(this, this, new Runnable() {
            @Override
            public void run() {
                setupAdView();
            }
        }).execute();
        if (!ApiKeyUtils.exists(this)) {
            new GetApiKeyTask(this).execute();
        }
    }

    private void switchCurrencyMenuTitle(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_currency);
        if (item == null) {
            return;
        }

        NavigationKind kind = viewController.getCurrentKind();
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

    public MainFragment getFragment() {
        return fragment;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            NavigationKind kind = viewController.getCurrentKind();
            if (kind != null && kind != NavigationKind.getDefault()) {
                viewController.setCurrentKind(NavigationKind.getDefault(), true);
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

        NavigationKind kind = viewController.getCurrentKind();
        if (clickedKind != null && kind != null && clickedKind != kind) {
            viewController.setCurrentKind(clickedKind, false);
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, PreferencesActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void requestRefreshUi(NavigationKind kind) {
        viewController.requestRefreshUi(kind);
    }

    private void setupAdView() {
        if (isPremiumPurchased()) {
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
    public List<NavigationKind> getVisibleKinds() {
        return viewController.getVisibleKinds();
    }

    @Override
    public TabLayout getTabLayout() {
        return findViewById(R.id.tab_layout);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewController.onDestroy();
        CKLog.releaseContext();
    }

    public void setFirebaseAnalytics(FirebaseAnalytics firebaseAnalytics) {
        this.firebaseAnalytics = firebaseAnalytics;
    }

    private static class InitializeActivityTask extends InitializeThirdPartyAppsTask {
        InitializeActivityTask(Context context, FirebaseAnalyticsReceiver receiver, Runnable runnable) {
            super(context, receiver, runnable);
        }

        @Override
        protected Void doInBackground(Void... params) {
            long start = System.currentTimeMillis();
            Fabric.with(context, new Crashlytics());
            receiver.setFirebaseAnalytics(FirebaseAnalytics.getInstance(context));
            MobileAds.initialize(context, BuildConfig.ADMOB_APP_ID);
            if (DEBUG) CKLog.d(TAG, "InitializeThirdPartyAppsTask() " + (System.currentTimeMillis() - start));

            return null;
        }
    }
}
