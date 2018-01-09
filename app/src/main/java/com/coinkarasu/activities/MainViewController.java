package com.coinkarasu.activities;

import android.app.Activity;

import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.Purchase;
import com.coinkarasu.billingmodule.billing.BillingManager;
import com.coinkarasu.billingmodule.skulist.row.TestItemDelegate;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.PrefHelper;

import java.util.List;

public class MainViewController {
    private static final boolean DEBUG = true;
    private static final String TAG = "MainViewController";

    private final UpdateListener updateListener;
    private Activity activity;
    private boolean isPremium;

    public MainViewController(Activity activity) {
        updateListener = new UpdateListener();
        this.activity = activity;
        loadData();
    }

    public UpdateListener getUpdateListener() {
        return updateListener;
    }

    public boolean isPremiumPurchased() {
        return isPremium;
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

    private void saveData() {
        PrefHelper.setPremium(activity, isPremium);
    }

    private void loadData() {
        isPremium = PrefHelper.isPremium(activity);
    }
}