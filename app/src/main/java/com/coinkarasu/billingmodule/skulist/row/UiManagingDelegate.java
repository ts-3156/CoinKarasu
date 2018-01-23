package com.coinkarasu.billingmodule.skulist.row;

import android.widget.Toast;

import com.android.billingclient.api.BillingClient.SkuType;
import com.coinkarasu.R;
import com.coinkarasu.billingmodule.billing.BillingProvider;

/**
 * Implementations of this abstract class are responsible to render UI and handle user actions for
 * skulist rows to render RecyclerView with AcquireFragment's specific UI
 */
public abstract class UiManagingDelegate {

    protected final BillingProvider mBillingProvider;

    public abstract @SkuType String getType();

    public UiManagingDelegate(BillingProvider billingProvider) {
        mBillingProvider = billingProvider;
    }

    public void onBindViewHolder(SkuRowData data, RowViewHolder holder) {
        holder.description.setText(data.getDescription());
        holder.price.setText(data.getPrice());
        holder.button.setEnabled(true);
    }

    public void onButtonClicked(SkuRowData data) {
        mBillingProvider.getBillingManager().initiatePurchaseFlow(data.getSku(), data.getSkuType());
    }

    protected void showAlreadyPurchasedToast() {
        Toast.makeText(mBillingProvider.getBillingManager().getContext(),
                R.string.billing_alert_already_purchased, Toast.LENGTH_SHORT).show();
    }
}
