package com.coinkarasu.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.transition.Fade;
import android.support.transition.Slide;
import android.support.transition.TransitionSet;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.coinkarasu.R;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.services.UpdateCoinListIntentService;
import com.coinkarasu.services.UpdateToplistIntentService;
import com.coinkarasu.services.UpdateTrendingIntentService;
import com.coinkarasu.tasks.InitializeThirdPartyAppsTask;
import com.coinkarasu.tasks.InsertLaunchEventTask;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.PrefHelper;
import com.coinkarasu.utils.Tutorial;
import com.google.firebase.analytics.FirebaseAnalytics;

public class FirstLaunchActivity extends AppCompatActivity implements
        InitializeThirdPartyAppsTask.FirebaseAnalyticsReceiver {
    private static final boolean DEBUG = true;
    private static final String TAG = "FirstLaunchActivity";

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentTransitionsFlag();
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
            setEnterTransition(nextFragment);
            setExitTransition(nextFragment);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, nextFragment)
                    .commit();
        }
    }

    private void setContentTransitionsFlag() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
    }

    private static final int DURATION = 300;

    private void setEnterTransition(Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TransitionSet set = new TransitionSet()
                    .addTransition(new Fade(Fade.IN))
                    .addTransition(new Slide(GravityCompat.END))
                    .setDuration(DURATION)
                    .setOrdering(TransitionSet.ORDERING_TOGETHER);

            fragment.setEnterTransition(set);
        }
    }

    private void setExitTransition(Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TransitionSet set = new TransitionSet()
                    .addTransition(new Slide(GravityCompat.START))
                    .setDuration(DURATION)
                    .setOrdering(TransitionSet.ORDERING_TOGETHER);

            fragment.setExitTransition(set);
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
