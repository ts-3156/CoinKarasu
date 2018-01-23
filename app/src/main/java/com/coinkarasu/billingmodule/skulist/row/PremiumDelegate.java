package com.coinkarasu.billingmodule.skulist.row;

import android.widget.Toast;

import com.android.billingclient.api.BillingClient.SkuType;
import com.coinkarasu.R;
import com.coinkarasu.billingmodule.billing.BillingProvider;

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
            holder.button.setText(R.string.billing_button_buy);
        }
    }

    @Override
    public void onButtonClicked(SkuRowData data) {
        if (mBillingProvider.isPremiumPurchased()) {
            showAlreadyPurchasedToast();
        } else if (mBillingProvider.isPremiumMonthly()) {
            Toast.makeText(mBillingProvider.getBillingManager().getContext(),
                    R.string.billing_alert_already_purchased, Toast.LENGTH_SHORT).show();
        } else {
            super.onButtonClicked(data);
        }
    }
}

