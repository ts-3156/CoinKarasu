package com.coinkarasu.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.AppLaunchChecker;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.R;
import com.coinkarasu.activities.etc.Currency;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.activities.settings.PreferencesActivity;
import com.coinkarasu.billingmodule.BillingViewController;
import com.coinkarasu.billingmodule.billing.BillingCallback;
import com.coinkarasu.billingmodule.billing.BillingManager;
import com.coinkarasu.tasks.GetApiKeyTask;
import com.coinkarasu.tasks.GetFirstLaunchDateTask;
import com.coinkarasu.tasks.InitializeThirdPartyAppsTask;
import com.coinkarasu.tasks.InsertLaunchEventTask;
import com.coinkarasu.utils.ApiKeyUtils;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.PrefHelper;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        MainFragment.OnFragmentInteractionListener,
        InitializeThirdPartyAppsTask.FirebaseAnalyticsReceiver,
        BillingCallback {

    private static final boolean DEBUG = true;
    private static final String TAG = "MainActivity";
    private static final String FRAGMENT_TAG = "fragment";

    private FirebaseAnalytics firebaseAnalytics;
    private MainViewController viewController;
    private BillingViewController billingDelegate;
    private BillingManager billingManager;
    private MainFragment fragment;
    private DrawerLayout drawer;
    private AdView ad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CKLog.setContext(this);
        new InsertLaunchEventTask().execute(this);
        new InitializeThirdPartyAppsTask().execute(this);

        if (PrefHelper.shouldShowFirstLaunchScreen(this) || !AppLaunchChecker.hasStartedFromLauncher(this)) {
            FirstLaunchActivity.start(this);
            AppLaunchChecker.onActivityCreate(this);
            finish();
            return;
        }

        setContentView(R.layout.activity_main); // ここで 180ms, onCreate全体で 230ms

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // FloatingActionButton fab = findViewById(R.id.fab);
        // fab.setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View view) {
        //         BillingActivity.start(view.getContext(), -1);
        //         Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        //                 .setAction("Action", null).show();
        //     }
        // });

        drawer = findViewById(R.id.drawer_layout);
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
        billingDelegate = new BillingViewController(this, null, this);
        billingManager = new BillingManager(this, billingDelegate.getUpdatesListener());

        if (!ApiKeyUtils.exists(this)) {
            new GetApiKeyTask(this).execute();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        if (viewController != null) {
            NavigationKind kind = viewController.getCurrentKind();
            if (kind != null && kind != NavigationKind.getDefault()) {
                viewController.setCurrentKind(NavigationKind.getDefault(), true);
                return;
            }
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
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

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * バックボタンが押された時、タブを編集した時などにMainFragmentからコールバックされる。
     */
    @Override
    public void requestRefreshUi(NavigationKind kind) {
        viewController.requestRefreshUi(kind);
    }

    @Override
    public void onBillingManagerSetupFinished() {
    }

    @Override
    public void onPurchasesUpdated() {
        setupAdView();
    }

    @Override
    public void onConsumeFinished() {
    }

    private void setupAdView() {
        if (isFinishing() || billingDelegate == null || billingDelegate.isPremium() || ad != null) {
            return;
        }

        new GetFirstLaunchDateTask(new GetFirstLaunchDateTask.Callback() {
            @Override
            public void run(Date date) {
                if (isFinishing() || ad != null || date == null
                        || System.currentTimeMillis() - date.getTime() < TimeUnit.DAYS.toMillis(3)) {
                    return;
                }

                final Activity activity = MainActivity.this;
                MobileAds.initialize(activity, BuildConfig.ADMOB_APP_ID);
                ad = new AdView(activity);
                ad.setAdSize(AdSize.SMART_BANNER);
                ad.setAdUnitId(BuildConfig.ADMOB_UNIT_ID);

                ad.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        if (isFinishing()) {
                            return;
                        }

                        if (ad.getParent() == null) {
                            ViewGroup parent = findViewById(R.id.main_ad_container);
                            parent.addView(ad);

                            int adHeight = AdSize.SMART_BANNER.getHeightInPixels(activity);
                            findViewById(R.id.fragment_container).setPadding(0, 0, 0, adHeight);

                            if (DEBUG) CKLog.d(TAG, "onAdLoaded()");
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        if (DEBUG) CKLog.w(TAG, "onAdFailedToLoad() " + errorCode);
                    }
                });
                ad.loadAd(new AdRequest.Builder().build());
            }
        }).execute(this);
    }

    /**
     * Pull-to-Refreshの際にCoinListFragmentとHomeTabFragmentから利用している。
     */
    public boolean isPremium() {
        return billingDelegate.isPremium();
    }

    public MainFragment getFragment() {
        return fragment;
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
    public void onResume() {
        super.onResume();
        CKLog.setContext(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        CKLog.releaseContext();
    }

    @Override
    protected void onDestroy() {
        if (billingManager != null) {
            billingManager.destroy();
        }
        if (viewController != null) {
            viewController.onDestroy();
        }
        if (billingDelegate != null) {
            billingDelegate.onDestroy();
        }
        super.onDestroy();
    }

    public void setFirebaseAnalytics(FirebaseAnalytics firebaseAnalytics) {
        this.firebaseAnalytics = firebaseAnalytics;
    }

    public FirebaseAnalytics getFirebaseAnalytics() {
        return firebaseAnalytics;
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }
}
