package com.coinkarasu.activities;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.Purchase;
import com.coinkarasu.R;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.billingmodule.billing.BillingManager;
import com.coinkarasu.billingmodule.skulist.row.TestItemDelegate;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.PrefHelper;

import java.util.List;

public class MainViewController implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final boolean DEBUG = true;
    private static final String TAG = "MainViewController";

    private final UpdateListener updateListener;
    private MainActivity activity;
    private boolean isPremium;

    public MainViewController(MainActivity activity) {
        updateListener = new UpdateListener();
        this.activity = activity;
        PrefHelper.getPref(activity).registerOnSharedPreferenceChangeListener(this);
        loadData();
    }

    public void setCurrentKind(NavigationKind kind, boolean smoothScroll) {
        MainFragment fragment = activity.getFragment();
        if (fragment != null) {
            fragment.setCurrentKind(kind, smoothScroll);
        }
    }

    public NavigationKind getCurrentKind() {
        MainFragment fragment = activity.getFragment();
        if (fragment == null) {
            return null;
        } else {
            return fragment.getCurrentKind();
        }
    }

    public void onPageChanged(NavigationKind kind) {
        setNavChecked(kind);
        updateToolbarTitle(kind);
        updateTabColor(kind);
        updateTabIconAlpha(kind);
    }

    private void setNavChecked(NavigationKind kind) {
        NavigationView view = activity.findViewById(R.id.nav_view);
        Menu menu = view.getMenu();

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);

            for (NavigationKind k : NavigationKind.values()) {
                if (item.getItemId() == k.navResId) {
                    item.setVisible(k.isVisible(activity));

                    if (k == kind) {
                        item.setChecked(true);
                        ColorStateList list = activity.getResources().getColorStateList(k.colorStateResId);
                        view.setItemTextColor(list);
                        view.setItemIconTintList(list);
                    }

                    break;
                }
            }
        }
    }

    private void updateToolbarTitle(NavigationKind kind) {
        ActionBar bar = activity.getSupportActionBar();
        if (bar == null) {
            return;
        }
        bar.setTitle(kind.headerStrResId);
    }

    private void updateTabColor(NavigationKind kind) {
        activity.findViewById(R.id.tab_layout).setBackgroundColor(activity.getResources().getColor(kind.colorResId));
        activity.findViewById(R.id.toolbar).setBackgroundColor(activity.getResources().getColor(kind.colorResId));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(activity, kind.colorDarkResId));
        }
    }

    private void updateTabIconAlpha(NavigationKind kind) {
        TabLayout tabs = activity.findViewById(R.id.tab_layout);
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals("pref_keep_screen_on")) {
            boolean isKeepScreenOn = prefs.getBoolean(key, activity.getResources().getBoolean(R.bool.keep_screen_on));
            if (isKeepScreenOn) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            if (DEBUG) CKLog.d(TAG, "keepScreenOn " + isKeepScreenOn);
        }
    }

    public UpdateListener getUpdateListener() {
        return updateListener;
    }

    public boolean isPremiumPurchased() {
        return isPremium;
    }

    public void onDestroy() {
        PrefHelper.getPref(activity).unregisterOnSharedPreferenceChangeListener(this);
        activity = null;
    }

    private void saveData() {
        PrefHelper.setPremium(activity, isPremium);
    }

    private void loadData() {
        isPremium = PrefHelper.isPremium(activity);
    }

    private class UpdateListener implements BillingManager.BillingUpdatesListener {
        @Override
        public void onBillingClientSetupFinished() {
        }

        @Override
        public void onConsumeFinished(String token, @BillingResponse int result) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onPurchasesUpdated(List<Purchase> purchaseList) {
            for (Purchase purchase : purchaseList) {
                switch (purchase.getSku()) {
                    case TestItemDelegate.SKU_ID:
                        if (DEBUG) CKLog.d(TAG, "You have a TestItem(Premium).");
                        isPremium = true;
                        saveData();
                        break;
                    default:
                        if (DEBUG) CKLog.e(TAG, "Not registered item " + purchase.getSku());
                }
            }
        }
    }
}