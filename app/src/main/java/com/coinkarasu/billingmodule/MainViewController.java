package com.coinkarasu.billingmodule;

import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.Purchase;
import com.coinkarasu.R;
import com.coinkarasu.billingmodule.billing.BillingManager;
import com.coinkarasu.billingmodule.skulist.row.PremiumDelegate;
import com.coinkarasu.billingmodule.skulist.row.TestItemDelegate;
import com.coinkarasu.billingmodule.skulist.row.TestSubscriptionDelegate;
import com.coinkarasu.utils.Log;

import java.util.List;

/**
 * Handles control logic of the BaseGamePlayActivity
 */
public class MainViewController {
    private static final boolean DEBUG = true;
    private static final String TAG = "MainViewController";

    // Graphics for the gas gauge
//    private static int[] TANK_RES_IDS = {com.example.billingmodule.R.drawable.gas0, com.example.billingmodule.R.drawable.gas1, com.example.billingmodule.R.drawable.gas2,
//            com.example.billingmodule.R.drawable.gas3, com.example.billingmodule.R.drawable.gas4};

    // How many units (1/4 tank is our unit) fill in the tank.
    private static final int TANK_MAX = 4;

    private final UpdateListener mUpdateListener;
    private BillingActivity mActivity;

    // Tracks if we currently own subscriptions SKUs
//    private boolean mGoldMonthly;
//    private boolean mGoldYearly;

    // Tracks if we currently own a premium car
    private boolean mIsPremium;

    // Current amount of gas in tank, in units
//    private int mTank;

    private Log logger;

    public MainViewController(BillingActivity activity) {
        mUpdateListener = new UpdateListener();
        mActivity = activity;
        logger = new Log(activity);
        loadData();
    }

    public void useGas() {
//        mTank--;
        saveData();
//        if (DEBUG) logger.d(TAG, "Tank is now: " + mTank);
    }

    public UpdateListener getUpdateListener() {
        return mUpdateListener;
    }

//    public boolean isTankEmpty() {
//        return mTank <= 0;
//    }
//
//    public boolean isTankFull() {
//        return mTank >= TANK_MAX;
//    }
//
//    public boolean isPremiumPurchased() {
//        return mIsPremium;
//    }
//
//    public boolean isGoldMonthlySubscribed() {
//        return mGoldMonthly;
//    }
//
//    public boolean isGoldYearlySubscribed() {
//        return mGoldYearly;
//    }

//    public @DrawableRes
//    int getTankResId() {
//        int index = (mTank >= TANK_RES_IDS.length) ? (TANK_RES_IDS.length - 1) : mTank;
//        return TANK_RES_IDS[index];
//    }

    /**
     * Handler to billing updates
     */
    private class UpdateListener implements BillingManager.BillingUpdatesListener {
        @Override
        public void onBillingClientSetupFinished() {
            mActivity.onBillingManagerSetupFinished();
        }

        @Override
        public void onConsumeFinished(String token, @BillingResponse int result) {
            if (DEBUG) logger.d(TAG, "Consumption finished. Purchase token: "
                    + token + ", result: " + result);

            // Note: We know this is the SKU_GAS, because it's the only one we consume, so we don't
            // check if token corresponding to the expected sku was consumed.
            // If you have more than one sku, you probably need to validate that the token matches
            // the SKU you expect.
            // It could be done by maintaining a map (updating it every time you call consumeAsync)
            // of all tokens into SKUs which were scheduled to be consumed and then looking through
            // it here to check which SKU corresponds to a consumed token.
            if (result == BillingResponse.OK) {
                // Successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                if (DEBUG) logger.d(TAG, "Consumption successful. Provisioning.");
//                mTank = mTank == TANK_MAX ? TANK_MAX : mTank + 1;
                saveData();
//                mActivity.alert(com.example.billingmodule.R.string.alert_fill_gas, mTank);
            } else {
                mActivity.alert(R.string.billing_alert_error_consuming, result);
            }

            mActivity.showRefreshedUi();
            if (DEBUG) logger.d(TAG, "End consumption flow.");
        }

        @Override
        public void onPurchasesUpdated(List<Purchase> purchaseList) {
//            mGoldMonthly = false;
//            mGoldYearly = false;

            for (Purchase purchase : purchaseList) {
                switch (purchase.getSku()) {
                    case PremiumDelegate.SKU_ID:
                        if (DEBUG) logger.d(TAG, "You are Premium! Congratulations!!!");
                        mIsPremium = true;
                        break;
                    case TestSubscriptionDelegate.SKU_ID:
                        if (DEBUG) logger.d(TAG, "You have a TestSubscription! Congratulations!!!");
                        mIsPremium = true;
                        break;
                    case TestItemDelegate.SKU_ID:
                        if (DEBUG) logger.d(TAG, "We have a TestItem. Consuming it.");
                        // We should consume the purchase and fill up the tank once it was consumed
                        mActivity.getBillingManager().consumeAsync(purchase.getPurchaseToken());
                        break;
//                    case GoldMonthlyDelegate.SKU_ID:
//                        mGoldMonthly = true;
//                        break;
//                    case GoldYearlyDelegate.SKU_ID:
//                        mGoldYearly = true;
//                        break;
                }
            }

            mActivity.showRefreshedUi();
        }
    }

    /**
     * Save current tank level to disc
     * <p>
     * Note: In a real application, we recommend you save data in a secure way to
     * prevent tampering.
     * For simplicity in this sample, we simply store the data using a
     * SharedPreferences.
     */
    private void saveData() {
//        SharedPreferences.Editor spe = mActivity.getPreferences(MODE_PRIVATE).edit();
//        spe.putInt("tank", mTank);
//        spe.apply();
//        Log.d(TAG, "Saved data: tank = " + String.valueOf(mTank));
    }

    private void loadData() {
//        SharedPreferences sp = mActivity.getPreferences(MODE_PRIVATE);
//        mTank = sp.getInt("tank", 2);
//        Log.d(TAG, "Loaded data: tank = " + String.valueOf(mTank));
    }
}