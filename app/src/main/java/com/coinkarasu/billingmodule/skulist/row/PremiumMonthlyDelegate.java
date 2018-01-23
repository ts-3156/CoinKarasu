package com.coinkarasu.billingmodule.skulist.row;

import com.android.billingclient.api.BillingClient.SkuType;
import com.coinkarasu.R;
import com.coinkarasu.billingmodule.billing.BillingProvider;

import java.util.ArrayList;

/**
 * Handles Ui specific to "premium" - non-consumable in-app item row
 */
public class PremiumMonthlyDelegate extends UiManagingDelegate {
    public static final String SKU_ID = "premium_monthly";

    public PremiumMonthlyDelegate(BillingProvider billingProvider) {
        super(billingProvider);
    }

    @Override
    public @SkuType
    String getType() {
        return SkuType.SUBS;
    }

    @Override
    public void onBindViewHolder(SkuRowData data, RowViewHolder holder) {
        super.onBindViewHolder(data, holder);
        if (mBillingProvider.isPremiumMonthly()) {
            holder.button.setText(R.string.billing_button_own);
        } else {
            int textId = mBillingProvider.isPremiumPurchased() ? R.string.billing_button_change : R.string.billing_button_buy;
            holder.button.setText(textId);
        }
    }

    @Override
    public void onButtonClicked(SkuRowData data) {
        if (mBillingProvider.isPremiumMonthly()) {
            showAlreadyPurchasedToast();
        } else if (mBillingProvider.isPremiumPurchased()) {
            ArrayList<String> currentSubscriptionSku = new ArrayList<>();
            currentSubscriptionSku.add(PremiumDelegate.SKU_ID);

            mBillingProvider.getBillingManager().initiatePurchaseFlow(data.getSku(),
                    currentSubscriptionSku, data.getSkuType());
        } else {
            super.onButtonClicked(data);
        }
    }
}

