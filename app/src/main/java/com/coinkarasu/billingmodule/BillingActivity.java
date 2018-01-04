package com.coinkarasu.billingmodule;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.coinkarasu.R;
import com.coinkarasu.billingmodule.billing.BillingManager;
import com.coinkarasu.billingmodule.billing.BillingProvider;
import com.coinkarasu.billingmodule.skulist.AcquireFragment;

import static com.android.billingclient.api.BillingClient.BillingResponse;
import static com.coinkarasu.billingmodule.billing.BillingManager.BILLING_MANAGER_NOT_INITIALIZED;

public class BillingActivity extends FragmentActivity implements BillingProvider {
    // Debug tag, for logging
    private static final String TAG = "BaseGamePlayActivity";

    // Tag for a dialog that allows us to find it when screen was rotated
    private static final String DIALOG_TAG = "dialog";

    private BillingManager mBillingManager;
    private AcquireFragment mAcquireFragment;
    private MainViewController mViewController;

    private View mScreenWait, mScreenMain;
    private ImageView mCarImageView, mGasImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_billing);

        // Start the controller and load game data
        mViewController = new MainViewController(this);

        // Try to restore dialog fragment if we were showing it prior to screen rotation
        if (savedInstanceState != null) {
            mAcquireFragment = (AcquireFragment) getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_TAG);
        }

        // Create and initialize BillingManager which talks to BillingLibrary
        mBillingManager = new BillingManager(this, mViewController.getUpdateListener());

        mScreenWait = findViewById(R.id.billing_screen_wait);
        mScreenMain = findViewById(R.id.billing_screen_main);
        mCarImageView = ((ImageView) findViewById(R.id.billing_free_or_premium));
        mGasImageView = ((ImageView) findViewById(R.id.billing_gas_gauge));

        // Specify purchase and drive buttons listeners
        // Note: This couldn't be done inside *.xml for Android TV since TV layout is inflated
        // via AppCompat
        findViewById(R.id.billing_button_purchase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPurchaseButtonClicked(view);
            }
        });
        findViewById(R.id.billing_button_drive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDriveButtonClicked(view);
            }
        });

        onPurchaseButtonClicked(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Note: We query purchases in onResume() to handle purchases completed while the activity
        // is inactive. For example, this can happen if the activity is destroyed during the
        // purchase flow. This ensures that when the activity is resumed it reflects the user's
        // current purchases.
        if (mBillingManager != null
                && mBillingManager.getBillingClientResponseCode() == BillingResponse.OK) {
            mBillingManager.queryPurchases();
        }
    }

    @Override
    public BillingManager getBillingManager() {
        return mBillingManager;
    }

    @Override
    public boolean isPremiumPurchased() {
//        return mViewController.isPremiumPurchased();
        return false;
    }

    @Override
    public boolean isGoldMonthlySubscribed() {
//        return mViewController.isGoldMonthlySubscribed();
        return false;
    }

    @Override
    public boolean isGoldYearlySubscribed() {
//        return mViewController.isGoldYearlySubscribed();
        return false;
    }

    @Override
    public boolean isTankFull() {
//        return mViewController.isTankFull();
        return false;
    }

    /**
     * User clicked the "Buy Gas" button - show a purchase dialog with all available SKUs
     */
    public void onPurchaseButtonClicked(final View arg0) {
        Log.d(TAG, "Purchase button clicked.");

        if (mAcquireFragment == null) {
            mAcquireFragment = new AcquireFragment();
        }

        if (!isAcquireFragmentShown()) {
            mAcquireFragment.show(getSupportFragmentManager(), DIALOG_TAG);

            if (mBillingManager != null
                    && mBillingManager.getBillingClientResponseCode()
                            > BILLING_MANAGER_NOT_INITIALIZED) {
                mAcquireFragment.onManagerReady(this);
            }
        }
    }

    /**
     * Drive button clicked. Burn gas!
     */
    public void onDriveButtonClicked(View arg0) {
        Log.d(TAG, "Drive button clicked.");

//        if (mViewController.isTankEmpty()) {
//            alert(R.string.alert_no_gas);
//        } else {
//            mViewController.useGas();
//            alert(R.string.alert_drove);
//            updateUi();
//        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroying helper.");
        if (mBillingManager != null) {
            mBillingManager.destroy();
        }
        super.onDestroy();
    }

    /**
     * Remove loading spinner and refresh the UI
     */
    public void showRefreshedUi() {
        setWaitScreen(false);
        updateUi();
        if (mAcquireFragment != null) {
            mAcquireFragment.refreshUI();
        }
    }

    /**
     * Show an alert dialog to the user
     * @param messageId String id to display inside the alert dialog
     */
    @UiThread
    void alert(@StringRes int messageId) {
        alert(messageId, null);
    }

    /**
     * Show an alert dialog to the user
     * @param messageId String id to display inside the alert dialog
     * @param optionalParam Optional attribute for the string
     */
    @UiThread
    void alert(@StringRes int messageId, @Nullable Object optionalParam) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new RuntimeException("Dialog could be shown only from the main thread");
        }

        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setNeutralButton("OK", null);

        if (optionalParam == null) {
            bld.setMessage(messageId);
        } else {
            bld.setMessage(getResources().getString(messageId, optionalParam));
        }

        bld.create().show();
    }

    void onBillingManagerSetupFinished() {
        if (mAcquireFragment != null) {
            mAcquireFragment.onManagerReady(this);
        }
    }

    @VisibleForTesting
    public MainViewController getViewController() {
        return mViewController;
    }

    /**
     * Enables or disables the "please wait" screen.
     */
    private void setWaitScreen(boolean set) {
        mScreenMain.setVisibility(set ? View.GONE : View.VISIBLE);
        mScreenWait.setVisibility(set ? View.VISIBLE : View.GONE);
    }

    /**
     * Sets image resource and also adds a tag to be able to verify that image is correct in tests
     */
    private void setImageResourceWithTestTag(ImageView imageView, @DrawableRes int resId) {
        imageView.setImageResource(resId);
        imageView.setTag(resId);
    }

    /**
     * Update UI to reflect model
     */
    @UiThread
    private void updateUi() {
        Log.d(TAG, "Updating the UI. Thread: " + Thread.currentThread().getName());

//        // Update car's color to reflect premium status or lack thereof
//        setImageResourceWithTestTag(mCarImageView, isPremiumPurchased() ? R.drawable.billpremium
//                : R.drawable.free);
//
//        // Update gas gauge to reflect tank status
//        setImageResourceWithTestTag(mGasImageView, mViewController.getTankResId());

        if (isGoldMonthlySubscribed() || isGoldYearlySubscribed()) {
            mCarImageView.setBackgroundColor(ContextCompat.getColor(this, R.color.billing_gold));
        }
    }

    public boolean isAcquireFragmentShown() {
        return mAcquireFragment != null && mAcquireFragment.isVisible();
    }

    public DialogFragment getDialogFragment() {
        return mAcquireFragment;
    }
}
