package com.coinkarasu.billingmodule;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StringRes;
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
import com.coinkarasu.billingmodule.billing.BillingCallback;
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
        BillingProvider,
        BillingCallback,
        InitializeThirdPartyAppsTask.FirebaseAnalyticsReceiver {

    private static final String TAG = "BillingActivity";
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

        showPrePurchaseDialog();
        updateToolbarColor();

        mViewController = new BillingViewController(this, this, this);
        mBillingManager = new BillingManager(this, mViewController.getUpdatesListener());

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
        return mViewController.isPremiumPurchased();
    }

    @Override
    public boolean isPremiumMonthly() {
        return mViewController.isPremiumMonthly();
    }

    @Override
    public boolean isPremium() {
        return mViewController.isPremium();
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
        if (mBillingManager != null) {
            mBillingManager.destroy();
        }
        if (mViewController != null) {
            mViewController.onDestroy();
        }
        super.onDestroy();
    }

    private void showPrePurchaseDialog() {
        Intent intent = getIntent();
        int resId = intent.getIntExtra("resId", -1);
        if (resId != -1) {
            BillingDialogFragment.newInstance(getString(resId))
                    .show(getSupportFragmentManager(), "dialog_tag");
        }
    }

    @Override
    public void onBillingManagerSetupFinished() {
        if (mRecyclerView != null) {
            setWaitScreen(true);
            querySkuDetails();
        }
    }

    @Override
    public void onPurchasesUpdated() {
        setWaitScreen(false);
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onConsumeFinished() {
        onPurchasesUpdated();
    }

    private void setWaitScreen(boolean set) {
        mRecyclerView.setVisibility(set ? View.GONE : View.VISIBLE);
        mScreenWait.setVisibility(set ? View.VISIBLE : View.GONE);
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
        if (isFinishing()) {
            return;
        }

        final List<SkuRowData> dataList = new ArrayList<>();
        mAdapter = new SkusAdapter();
        final UiManager uiManager = createUiManager(mAdapter, this);
        mAdapter.setUiManager(uiManager);

        List<String> subscriptionsSkus = uiManager.getDelegatesFactory().getSkuList(BillingClient.SkuType.SUBS);
        addSkuRows(dataList, subscriptionsSkus, BillingClient.SkuType.SUBS, new Runnable() {
            @Override
            public void run() {
                List<String> inAppSkus = uiManager.getDelegatesFactory().getSkuList(BillingClient.SkuType.INAPP);
                addSkuRows(dataList, inAppSkus, BillingClient.SkuType.INAPP, null);
            }
        });
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

                    for (SkuDetails details : skuDetailsList) {
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
