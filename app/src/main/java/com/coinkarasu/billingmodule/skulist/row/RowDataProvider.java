package com.coinkarasu.billingmodule.skulist.row;


/**
 * Provider for data that corresponds to a particular row
 */
public interface RowDataProvider {
    SkuRowData getData(int position);
}

