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
            holder.button.setText(R.string.billing_button_buy);
        }
    }

    @Override
    public void onButtonClicked(SkuRowData data) {
        if (mBillingProvider.isPremiumMonthly()) {
            showAlreadyPurchasedToast();
        } else if (mBillingProvider.isPremiumPurchased()) {
            showAlreadyPurchasedToast();
        } else {
            super.onButtonClicked(data);
        }
    }
}

