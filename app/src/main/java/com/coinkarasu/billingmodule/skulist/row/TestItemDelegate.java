package com.coinkarasu.billingmodule.skulist.row;

import com.android.billingclient.api.BillingClient.SkuType;
import com.coinkarasu.R;
import com.coinkarasu.billingmodule.billing.BillingProvider;

/**
 * Handles Ui specific to "premium" - non-consumable in-app item row
 */
public class TestItemDelegate extends UiManagingDelegate {
    public static final String SKU_ID = "test_item";

    public TestItemDelegate(BillingProvider billingProvider) {
        super(billingProvider);
    }

    @Override
    public @SkuType String getType() {
        return SkuType.INAPP;
    }

    @Override
    public void onBindViewHolder(SkuRowData data, RowViewHolder holder) {
        super.onBindViewHolder(data, holder);
        int textId = mBillingProvider.isPremiumPurchased() ? R.string.billing_button_own
                : R.string.billing_button_buy;
        holder.button.setText(textId);
//        holder.skuIcon.setImageResource(R.drawable.ic_diamond);
    }

    @Override
    public void onButtonClicked(SkuRowData data) {
        if (data != null && mBillingProvider.isPremiumPurchased()) {
            showAlreadyPurchasedToast();
        }  else {
            super.onButtonClicked(data);
        }
    }
}

