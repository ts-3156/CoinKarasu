package com.coinkarasu.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.coinkarasu.R;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.services.GetApiKeyIntentService;
import com.coinkarasu.services.UpdateCoinListIntentService;
import com.coinkarasu.services.UpdateToplistIntentService;
import com.coinkarasu.services.UpdateTrendingIntentService;
import com.coinkarasu.tasks.InitializeThirdPartyAppsTask;
import com.coinkarasu.tasks.InsertLaunchEventTask;
import com.coinkarasu.utils.ApiKeyUtils;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.PrefHelper;
import com.coinkarasu.utils.TransitionUtils;
import com.coinkarasu.utils.Tutorial;
import com.google.firebase.analytics.FirebaseAnalytics;

public class FirstLaunchActivity extends AppCompatActivity implements
        InitializeThirdPartyAppsTask.FirebaseAnalyticsReceiver {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "FirstLaunchActivity";

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionUtils.setContentTransitionsFlag(this);
        setContentView(R.layout.activity_first_launch);

        CKLog.setContext(this);
        new InsertLaunchEventTask().execute(this);
        new InitializeThirdPartyAppsTask(new Runnable() {
            @Override
            public void run() {
                Tutorial.logTutorialBegin(firebaseAnalytics);
            }
        }).execute(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, NavigationKind.edit_tabs.colorDarkResId));
        }

        goToNext(null);

        if (savedInstanceState == null) {
            UpdateCoinListIntentService.start(this);
            UpdateTrendingIntentService.start(this, false);
            for (NavigationKind kind : NavigationKind.values()) {
                if (kind.isToplist() && kind.isVisible(this)) {
                    UpdateToplistIntentService.start(this, kind);
                }
            }
        }

        if (!ApiKeyUtils.exists(this)) {
            GetApiKeyIntentService.start(this);
        }

        PrefHelper.setShouldShowFirstLaunchScreen(this, false);
    }

    public void goToNext(Fragment currentFragment) {
        if (DEBUG) CKLog.d(TAG, "goToNext() " + (currentFragment == null ? "null" : currentFragment.toString()));
        Fragment nextFragment = null;

        if (currentFragment == null) {
            nextFragment = FirstLaunchSplashFragment.newInstance();
        } else if (currentFragment instanceof FirstLaunchSplashFragment) {
            // nextFragment = FirstLaunchSplashFragment.newInstance();
        }

        if (nextFragment == null) {
            MainActivity.start(this);
            finish();
        } else {
            TransitionUtils.setSlideEnterTransition(nextFragment);
            TransitionUtils.setSlideExitTransition(nextFragment);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, nextFragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        CKLog.releaseContext();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, FirstLaunchActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void setFirebaseAnalytics(FirebaseAnalytics firebaseAnalytics) {
        this.firebaseAnalytics = firebaseAnalytics;
    }

}
