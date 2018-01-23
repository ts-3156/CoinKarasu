package com.coinkarasu.billingmodule.billing;


public interface BillingProvider {
    BillingManager getBillingManager();

    boolean isPremiumPurchased();

    boolean isPremiumMonthly();

    boolean isPremium();
}

