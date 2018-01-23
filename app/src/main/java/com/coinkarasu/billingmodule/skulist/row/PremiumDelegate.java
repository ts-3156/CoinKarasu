package com.coinkarasu.billingmodule.skulist.row;

import com.android.billingclient.api.BillingClient.SkuType;
import com.coinkarasu.R;
import com.coinkarasu.billingmodule.billing.BillingProvider;

import java.util.ArrayList;

/**
 * Handles Ui specific to "premium" - non-consumable in-app item row
 */
public class PremiumDelegate extends UiManagingDelegate {
    public static final String SKU_ID = "premium";

    public PremiumDelegate(BillingProvider billingProvider) {
        super(billingProvider);
    }

    @Override
    public @SkuType
    String getType() {
        return SkuType.INAPP;
    }

    @Override
    public void onBindViewHolder(SkuRowData data, RowViewHolder holder) {
        super.onBindViewHolder(data, holder);
        if (mBillingProvider.isPremiumPurchased()) {
            holder.button.setText(R.string.billing_button_own);
        } else {
            int textId = mBillingProvider.isPremiumMonthly() ? R.string.billing_button_change : R.string.billing_button_buy;
            holder.button.setText(textId);
        }
    }

    @Override
    public void onButtonClicked(SkuRowData data) {
        if (mBillingProvider.isPremiumPurchased()) {
            showAlreadyPurchasedToast();
        } else if (mBillingProvider.isPremiumMonthly()) {
            ArrayList<String> currentSubscriptionSku = new ArrayList<>();
            currentSubscriptionSku.add(PremiumMonthlyDelegate.SKU_ID);

            mBillingProvider.getBillingManager().initiatePurchaseFlow(data.getSku(),
                    currentSubscriptionSku, data.getSkuType());
        } else {
            super.onButtonClicked(data);
        }
    }
}

