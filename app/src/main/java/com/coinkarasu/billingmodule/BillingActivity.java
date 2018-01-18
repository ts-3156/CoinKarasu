package com.coinkarasu.billingmodule;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.coinkarasu.R;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.billingmodule.billing.BillingManager;
import com.coinkarasu.billingmodule.billing.BillingProvider;
import com.coinkarasu.billingmodule.skulist.CardsWithHeadersDecoration;
import com.coinkarasu.billingmodule.skulist.SkusAdapter;
import com.coinkarasu.billingmodule.skulist.row.SkuRowData;
import com.coinkarasu.billingmodule.skulist.row.UiManager;
import com.coinkarasu.tasks.InitializeThirdPartyAppsTask;
import com.coinkarasu.tasks.InsertLaunchEventTask;
import com.coinkarasu.utils.CKLog;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import static com.android.billingclient.api.BillingClient.BillingResponse;
import static com.coinkarasu.billingmodule.billing.BillingManager.BILLING_MANAGER_NOT_INITIALIZED;

public class BillingActivity extends AppCompatActivity implements
        BillingProvider, InitializeThirdPartyAppsTask.FirebaseAnalyticsReceiver {

    private static final String TAG = "BaseGamePlayActivity";
    private static final boolean DEBUG = true;

    private BillingManager mBillingManager;
    private BillingViewController mViewController;

    private RecyclerView mRecyclerView;
    private SkusAdapter mAdapter;
    private TextView mErrorTextView;
    private View mScreenWait;

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        CKLog.setContext(this);
        new InsertLaunchEventTask().execute(this);
        new InitializeThirdPartyAppsTask().execute(this);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(R.string.billing_button_purchase);
        }

        showDialog();
        updateToolbarColor();

        mViewController = new BillingViewController(this);
        mBillingManager = new BillingManager(this, mViewController.getUpdateListener());

        mErrorTextView = findViewById(R.id.error_textview);
        mRecyclerView = findViewById(R.id.list);
        mScreenWait = findViewById(R.id.billing_screen_wait);

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
        if (DEBUG) CKLog.d(TAG, "Purchase button clicked.");

        if (mBillingManager != null
                && mBillingManager.getBillingClientResponseCode() > BILLING_MANAGER_NOT_INITIALIZED) {
            if (mRecyclerView != null) {
                setWaitScreen(true);
                querySkuDetails();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        CKLog.releaseContext();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) CKLog.d(TAG, "Destroying helper.");
        if (mBillingManager != null) {
            mBillingManager.destroy();
        }
        super.onDestroy();
    }

    private void showDialog() {
        Intent intent = getIntent();
        int resId = intent.getIntExtra("resId", -1);
        if (resId != -1) {
            BillingDialogFragment.newInstance(getString(resId))
                    .show(getSupportFragmentManager(), "dialog_tag");
        }
    }

    /**
     * Remove loading spinner and refresh the UI
     */
    public void showRefreshedUi() {
        setWaitScreen(false);
        updateUi();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Show an alert dialog to the user
     *
     * @param messageId     String id to display inside the alert dialog
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
        if (mRecyclerView != null) {
            setWaitScreen(true);
            querySkuDetails();

        }
    }

    @VisibleForTesting
    public BillingViewController getViewController() {
        return mViewController;
    }

    /**
     * Enables or disables the "please wait" screen.
     */
    private void setWaitScreen(boolean set) {
        mRecyclerView.setVisibility(set ? View.GONE : View.VISIBLE);
        mScreenWait.setVisibility(set ? View.VISIBLE : View.GONE);
    }

    /**
     * Update UI to reflect model
     */
    @UiThread
    private void updateUi() {
        if (DEBUG) CKLog.d(TAG, "Updating the UI. Thread: " + Thread.currentThread().getName());

//        // Update car's color to reflect premium status or lack thereof
//        setImageResourceWithTestTag(mCarImageView, isPremiumPurchased() ? R.drawable.billpremium
//                : R.drawable.free);
//
//        // Update gas gauge to reflect tank status
//        setImageResourceWithTestTag(mGasImageView, mViewController.getTankResId());

        if (isGoldMonthlySubscribed() || isGoldYearlySubscribed()) {
//            mCarImageView.setBackgroundColor(ContextCompat.getColor(this, R.color.billing_gold));
        }
    }

    private void displayAnErrorIfNeeded() {
        if (isFinishing()) {
            if (DEBUG) CKLog.i(TAG, "No need to show an error - activity is finishing already");
            return;
        }

        mScreenWait.setVisibility(View.GONE);
        mErrorTextView.setVisibility(View.VISIBLE);
        int billingResponseCode = getBillingManager()
                .getBillingClientResponseCode();

        switch (billingResponseCode) {
            case BillingResponse.OK:
                // If manager was connected successfully, then show no SKUs error
                mErrorTextView.setText(getText(R.string.billing_error_no_skus));
                break;
            case BillingResponse.BILLING_UNAVAILABLE:
                mErrorTextView.setText(getText(R.string.billing_error_billing_unavailable));
                break;
            default:
                mErrorTextView.setText(getText(R.string.billing_error_billing_default));
        }

    }

    /**
     * Queries for in-app and subscriptions SKU details and updates an adapter with new data
     */
    private void querySkuDetails() {
        long startTime = System.currentTimeMillis();

        if (DEBUG)
            CKLog.d(TAG, "querySkuDetails() got subscriptions and inApp SKU details lists for: "
                    + (System.currentTimeMillis() - startTime) + "ms");

        if (!isFinishing()) {
            final List<SkuRowData> dataList = new ArrayList<>();
            mAdapter = new SkusAdapter();
            final UiManager uiManager = createUiManager(mAdapter, this);
            mAdapter.setUiManager(uiManager);
            // Filling the list with all the data to render subscription rows
            List<String> subscriptionsSkus = uiManager.getDelegatesFactory().getSkuList(BillingClient.SkuType.SUBS);
            addSkuRows(dataList, subscriptionsSkus, BillingClient.SkuType.SUBS, new Runnable() {
                @Override
                public void run() {
                    // Once we added all the subscription items, fill the in-app items rows below
                    List<String> inAppSkus = uiManager.getDelegatesFactory().getSkuList(BillingClient.SkuType.INAPP);
                    addSkuRows(dataList, inAppSkus, BillingClient.SkuType.INAPP, null);
                }
            });
        }
    }

    private void addSkuRows(final List<SkuRowData> inList, List<String> skusList, final @BillingClient.SkuType String billingType, final Runnable executeWhenFinished) {

        getBillingManager().querySkuDetailsAsync(billingType, skusList, new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {

                if (responseCode != BillingResponse.OK) {
                    if (DEBUG) CKLog.w(TAG, "Unsuccessful query for type: "
                            + billingType + ". Error code: " + responseCode);
                } else if (skuDetailsList != null && skuDetailsList.size() > 0) {
                    // If we successfully got SKUs, add a header in front of the row
                    @StringRes int stringRes = (billingType.equals(BillingClient.SkuType.INAPP)) ? R.string.billing_header_inapp : R.string.billing_header_subscriptions;
                    inList.add(new SkuRowData(getString(stringRes)));
                    // Then fill all the other rows
                    for (SkuDetails details : skuDetailsList) {
                        if (DEBUG) CKLog.i(TAG, "Adding sku: " + details.getSku());
                        inList.add(new SkuRowData(details, SkusAdapter.TYPE_NORMAL, billingType));
                    }

                    if (inList.size() == 0) {
                        displayAnErrorIfNeeded();
                    } else {
                        if (mRecyclerView.getAdapter() == null) {
                            mRecyclerView.setAdapter(mAdapter);
                            Resources res = getResources();
                            mRecyclerView.addItemDecoration(new CardsWithHeadersDecoration(mAdapter,
                                    res.getDimensionPixelSize(R.dimen.billing_header_gap),
                                    res.getDimensionPixelSize(R.dimen.billing_row_gap)));
                            mRecyclerView.setLayoutManager(new LinearLayoutManager(BillingActivity.this));
                        }

                        mAdapter.updateData(inList);
                        setWaitScreen(false);
                    }

                }

                if (executeWhenFinished != null) {
                    executeWhenFinished.run();
                }
            }
        });
    }

    protected UiManager createUiManager(SkusAdapter adapter, BillingProvider provider) {
        return new UiManager(adapter, provider);
    }


    private void updateToolbarColor() {
        NavigationKind kind = NavigationKind.edit_tabs;
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(kind.colorResId)));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, kind.colorDarkResId));
        }
    }

    public static void start(Context context, int resId) {
        Intent intent = new Intent(context, BillingActivity.class);
        intent.putExtra("resId", resId);
        context.startActivity(intent);
    }

    @Override
    public void setFirebaseAnalytics(FirebaseAnalytics firebaseAnalytics) {
        this.firebaseAnalytics = firebaseAnalytics;
    }

}
