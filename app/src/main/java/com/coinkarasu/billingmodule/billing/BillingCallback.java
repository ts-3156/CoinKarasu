package com.coinkarasu.billingmodule.billing;


public interface BillingCallback {
    void onBillingManagerSetupFinished();

    void onPurchasesUpdated();

    void onConsumeFinished();
}

