package com.coinkarasu.billingmodule.skulist.row;

import com.android.billingclient.api.BillingClient.SkuType;
import com.coinkarasu.billingmodule.billing.BillingProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This factory is responsible to finding the appropriate delegate for Ui rendering and calling
 * corresponding method on it.
 */
public class UiDelegatesFactory {
    private final Map<String, UiManagingDelegate> uiDelegates;

    public UiDelegatesFactory(BillingProvider provider) {
        uiDelegates = new HashMap<>();
//        uiDelegates.put(GasDelegate.SKU_ID, new GasDelegate(provider));
//        uiDelegates.put(GoldMonthlyDelegate.SKU_ID, new GoldMonthlyDelegate(provider));
//        uiDelegates.put(GoldYearlyDelegate.SKU_ID, new GoldYearlyDelegate(provider));
//        uiDelegates.put(PremiumDelegate.SKU_ID, new PremiumDelegate(provider));
        uiDelegates.put(TestItemDelegate.SKU_ID, new TestItemDelegate(provider));
        uiDelegates.put(TestSubscriptionDelegate.SKU_ID, new TestSubscriptionDelegate(provider));
    }

    /**
     * Returns the list of all SKUs for the billing type specified
     */
    public final List<String> getSkuList(@SkuType String billingType) {
        List<String> result = new ArrayList<>();
        for (String skuId : uiDelegates.keySet()) {
            UiManagingDelegate delegate = uiDelegates.get(skuId);
            if (delegate.getType().equals(billingType)) {
                result.add(skuId);
            }
        }
        return result;
    }

    public void onBindViewHolder(SkuRowData data, RowViewHolder holder) {
        uiDelegates.get(data.getSku()).onBindViewHolder(data, holder);
    }

    public void onButtonClicked(SkuRowData data) {
        uiDelegates.get(data.getSku()).onButtonClicked(data);
    }
}
