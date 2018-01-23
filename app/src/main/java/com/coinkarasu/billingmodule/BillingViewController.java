package com.coinkarasu.billingmodule;

import android.content.Context;

import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.Purchase;
import com.coinkarasu.billingmodule.billing.BillingCallback;
import com.coinkarasu.billingmodule.billing.BillingManager;
import com.coinkarasu.billingmodule.skulist.row.PremiumDelegate;
import com.coinkarasu.billingmodule.skulist.row.PremiumMonthlyDelegate;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.PrefHelper;

import java.util.List;

public class BillingViewController {
    private static final boolean DEBUG = true;
    private static final String TAG = "BillingViewController";

    private Context context;
    private BillingCallback billingCallback;
    private UpdatesListener updatesListener;

    public BillingViewController(Context context, BillingCallback billingCallback) {
        this.context = context;
        this.billingCallback = billingCallback;
        updatesListener = new UpdatesListener();
    }

    public UpdatesListener getUpdatesListener() {
        return updatesListener;
    }

    public boolean isPremium() {
        return PrefHelper.isDebugPremium(context) || PrefHelper.isPremium(context);
    }

    public boolean isPremiumMonthly() {
        return PrefHelper.isPremiumMonthly(context);
    }

    public boolean isPremiumPurchased() {
        return PrefHelper.isPremiumPurchased(context);
    }

    private class UpdatesListener implements BillingManager.BillingUpdatesListener {
        @Override
        public void onBillingClientSetupFinished() {
            billingCallback.onBillingManagerSetupFinished();
        }

        @Override
        public void onConsumeFinished(String token, @BillingResponse int result) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onPurchasesUpdated(List<Purchase> purchaseList) {
            boolean isPremiumPurchased = false;
            boolean premiumMonthly = false;

            for (Purchase purchase : purchaseList) {
                switch (purchase.getSku()) {
                    case PremiumDelegate.SKU_ID:
                        if (DEBUG) CKLog.d(TAG, "You have a premium item.");
                        isPremiumPurchased = true;
                        break;
                    case PremiumMonthlyDelegate.SKU_ID:
                        if (DEBUG) CKLog.d(TAG, "You have a premium subscription.");
                        premiumMonthly = true;
                        break;
                    default:
                        if (DEBUG) CKLog.w(TAG, "Not registered item " + purchase.getSku());
                }
            }

            PrefHelper.setPremiumPurchased(context, isPremiumPurchased);
            PrefHelper.setPremiumMonthly(context, premiumMonthly);

            billingCallback.onPurchasesUpdated();
        }
    }

    public void onDestroy() {
        context = null;
        billingCallback = null;
    }
}