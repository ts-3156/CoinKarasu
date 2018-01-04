package com.coinkarasu.billingmodule.skulist.row;

import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.SkuDetails;
import com.coinkarasu.billingmodule.skulist.SkusAdapter;

/**
 * A model for SkusAdapter's row
 */
public class SkuRowData {
    private String sku, title, price, description;
    private int type;
    private String billingType;

    public SkuRowData(SkuDetails details, int rowType, String billingType) {
        this.sku = details.getSku();
        this.title = details.getTitle();
        this.price = details.getPrice();
        this.description = details.getDescription();
        this.type = rowType;
        this.billingType = billingType;
    }

    public SkuRowData(String title) {
        this.title = title;
        this.type = SkusAdapter.TYPE_HEADER;
    }

    public String getSku() {
        return sku;
    }

    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    //    public @RowTypeDef int getRowType() {
    public int getRowType() {
        return type;
    }

    public @SkuType
    String getSkuType() {
        return billingType;
    }
}
