package com.coinkarasu.billingmodule;

import android.content.Context;

import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.Purchase;
import com.coinkarasu.R;
import com.coinkarasu.billingmodule.billing.BillingCallback;
import com.coinkarasu.billingmodule.billing.BillingManager;
import com.coinkarasu.billingmodule.billing.BillingProvider;
import com.coinkarasu.billingmodule.skulist.row.PremiumDelegate;
import com.coinkarasu.billingmodule.skulist.row.PremiumMonthlyDelegate;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.PrefHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BillingViewController {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "BillingViewController";

    private Context context;
    private BillingProvider billingProvider;
    private BillingCallback billingCallback;
    private UpdatesListener updatesListener;
    private Set<String> tokensToBeConsumed;

    public BillingViewController(Context context, BillingProvider billingProvider, BillingCallback billingCallback) {
        this.context = context;
        this.billingProvider = billingProvider;
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
            if (tokensToBeConsumed == null || !tokensToBeConsumed.contains(token)) {
                return;
            }

            if (result == BillingResponse.OK) {
                if (DEBUG) CKLog.d(TAG, "Consumption successful. Provisioning. " + token);
                PrefHelper.setPremiumPurchased(context, false);
            } else {
                if (DEBUG) CKLog.w(TAG, context.getString(R.string.billing_alert_error_consuming, result));
            }

            billingCallback.onConsumeFinished();
        }

        @Override
        public void onPurchasesUpdated(List<Purchase> purchaseList) {
            boolean isPremiumPurchased = false;
            boolean premiumMonthly = false;
            boolean isForceConsumeItems = PrefHelper.isForceConsumeItems(context);

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

                if (isForceConsumeItems && purchase.getSku().equals(PremiumDelegate.SKU_ID) && billingProvider != null) {
                    String token = purchase.getPurchaseToken();
                    if (tokensToBeConsumed == null) {
                        tokensToBeConsumed = new HashSet<>();
                    }
                    if (!tokensToBeConsumed.contains(token)) {
                        tokensToBeConsumed.add(token);
                        billingProvider.getBillingManager().consumeAsync(token);
                    }
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
