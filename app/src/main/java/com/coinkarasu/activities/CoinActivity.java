package com.coinkarasu.activities;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.coinkarasu.R;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.custom.SwipeDetector;
import com.coinkarasu.tasks.InitializeThirdPartyAppsTask;
import com.coinkarasu.tasks.InsertLaunchEventTask;
import com.coinkarasu.tasks.LogCoinSelectedEventTask;
import com.coinkarasu.utils.CKLog;
import com.google.firebase.analytics.FirebaseAnalytics;

public class CoinActivity extends AppCompatActivity implements
        InitializeThirdPartyAppsTask.FirebaseAnalyticsReceiver {

    private static final boolean DEBUG = true;
    private static final String TAG = "CoinActivity";
    private static final String KEY_COIN_JSON = "KEY_COIN_JSON";
    private static final String KEY_KIND = "KEY_KIND";

    private Coin coin;

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin);

        Intent intent = getIntent();
        coin = Coin.buildBy(intent.getStringExtra(KEY_COIN_JSON));
        final NavigationKind kind = NavigationKind.valueOf(intent.getStringExtra(KEY_KIND));

        CKLog.setContext(this);
        new InsertLaunchEventTask().execute(this);
        new InitializeThirdPartyAppsTask(new Runnable() {
            @Override
            public void run() {
                new LogCoinSelectedEventTask().execute(CoinActivity.this, kind, coin);
            }
        }).execute(this);

        setToolbarTitle(kind);
        setToolbarColor(kind);

        if (savedInstanceState == null) {
            setupFragments();
        }
    }

    private void setupFragments() {
        String toSymbol = coin.getToSymbol();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.price_overview, PriceOverviewFragment.newInstance(coin), "price_overview")
                .replace(R.id.historical_price, HistoricalPriceFragment.newInstance(coin.getSymbol(), toSymbol), "historical_price")
                .replace(R.id.card_exchange, CoinExchangeFragment.newInstance("overview", coin.toJson().toString()), "coin_exchange")
                .replace(R.id.card_pie_chart, CoinPieChartFragment.newInstance(coin.getSymbol(), toSymbol), "coin_pie_chart")
                .replace(R.id.card_board, CoinBoardFragment.newInstance("order_book"), "coin_board")
                .commit();

        SwipeDetector detector = new SwipeDetector(this);
        detector.attach(findViewById(R.id.linear_layout));
        detector.setOnSwipeListener(new SwipeDetector.OnSwipeListener() {
            @Override
            public void onSwipe(View view, int direction) {
                if (SwipeDetector.TO_RIGHT == direction) {
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_enter_from_left, R.anim.activity_exit_to_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.coin, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setToolbarTitle(NavigationKind kind) {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(coin.getFullName());
            bar.setSubtitle(getString(kind.tabStrResId) + " " + kind.getToSymbol());
        }
    }

    private void setToolbarColor(NavigationKind kind) {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(kind.colorResId)));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, kind.colorDarkResId));
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_enter_from_left, R.anim.activity_exit_to_right);
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

    public static void start(Context context, Coin coin, NavigationKind kind) {
        Intent intent = new Intent(context, CoinActivity.class);
        intent.putExtra(KEY_COIN_JSON, coin.toJson().toString());
        intent.putExtra(KEY_KIND, kind.name());
        context.startActivity(intent);

        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else if (context instanceof ContextWrapper) {
            Context ctx = ((ContextWrapper) context).getBaseContext();
            if (ctx instanceof Activity) {
                activity = (Activity) ctx;
            }
        }

        if (activity != null) {
            activity.overridePendingTransition(R.anim.activity_enter_from_right, R.anim.activity_exit_to_left);
        }
    }

    @Override
    public void setFirebaseAnalytics(FirebaseAnalytics firebaseAnalytics) {
        this.firebaseAnalytics = firebaseAnalytics;
    }

    public FirebaseAnalytics getFirebaseAnalytics() {
        return firebaseAnalytics;
    }

}
