package com.coinkarasu.billingmodule.skulist;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.coinkarasu.R;
import com.coinkarasu.billingmodule.billing.BillingProvider;
import com.coinkarasu.billingmodule.skulist.row.SkuRowData;
import com.coinkarasu.billingmodule.skulist.row.UiManager;
import com.coinkarasu.utils.Log;

import java.util.ArrayList;
import java.util.List;

import static com.android.billingclient.api.BillingClient.BillingResponse;

/**
 * Displays a screen with various in-app purchase and subscription options
 */
public class AcquireFragment extends DialogFragment {
    private static final boolean DEBUG = true;
    private static final String TAG = "AcquireFragment";

    private RecyclerView mRecyclerView;
    private SkusAdapter mAdapter;
    private View mLoadingView;
    private TextView mErrorTextView;
    private BillingProvider mBillingProvider;
    private boolean mWasRetryServiceConnection;

    // onCreateViewよりもonManagerReadyの方が先に呼ばれる可能性があるため、
    // ここで初期化している。
    private Log logger;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BillingAppTheme);

        logger = new Log(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.billing_acquire_fragment, container, false);
        mErrorTextView = root.findViewById(R.id.error_textview);
        mRecyclerView = root.findViewById(R.id.list);
        mLoadingView = root.findViewById(R.id.billing_screen_wait);
        if (mBillingProvider != null) {
            handleManagerAndUiReady();
        }
        // Setup a toolbar for this fragment
        Toolbar toolbar = root.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.billing_ic_arrow_up);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        toolbar.setTitle(R.string.billing_button_purchase);

        return root;
    }

    /**
     * Refreshes this fragment's UI
     */
    public void refreshUI() {
        if (DEBUG)
            logger.d(TAG, "Looks like purchases list might have been updated - refreshing the UI");
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Notifies the fragment that billing manager is ready and provides a BillingProviders
     * instance to access it
     */
    public void onManagerReady(BillingProvider billingProvider) {
        mBillingProvider = billingProvider;
        if (mRecyclerView != null) {
            handleManagerAndUiReady();
        }
    }

    /**
     * Enables or disables "please wait" screen.
     */
    private void setWaitScreen(boolean set) {
        mRecyclerView.setVisibility(set ? View.GONE : View.VISIBLE);
        mLoadingView.setVisibility(set ? View.VISIBLE : View.GONE);
    }

    /**
     * Executes query for SKU details at the background thread
     */
    private void handleManagerAndUiReady() {
        // If Billing Manager was successfully initialized - start querying for SKUs
        setWaitScreen(true);
        querySkuDetails();
    }

    private void displayAnErrorIfNeeded() {
        if (getActivity() == null || getActivity().isFinishing()) {
            if (DEBUG) logger.i(TAG, "No need to show an error - activity is finishing already");
            return;
        }

        mLoadingView.setVisibility(View.GONE);
        mErrorTextView.setVisibility(View.VISIBLE);
        int billingResponseCode = mBillingProvider.getBillingManager()
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
            logger.d(TAG, "querySkuDetails() got subscriptions and inApp SKU details lists for: "
                    + (System.currentTimeMillis() - startTime) + "ms");

        if (getActivity() != null && !getActivity().isFinishing()) {
            final List<SkuRowData> dataList = new ArrayList<>();
            mAdapter = new SkusAdapter();
            final UiManager uiManager = createUiManager(mAdapter, mBillingProvider);
            mAdapter.setUiManager(uiManager);
            // Filling the list with all the data to render subscription rows
            List<String> subscriptionsSkus = uiManager.getDelegatesFactory().getSkuList(SkuType.SUBS);
            addSkuRows(dataList, subscriptionsSkus, SkuType.SUBS, new Runnable() {
                @Override
                public void run() {
                    // Once we added all the subscription items, fill the in-app items rows below
                    List<String> inAppSkus = uiManager.getDelegatesFactory().getSkuList(SkuType.INAPP);
                    addSkuRows(dataList, inAppSkus, SkuType.INAPP, null);
                }
            });
        }
    }

    private void addSkuRows(final List<SkuRowData> inList, List<String> skusList, final @SkuType String billingType, final Runnable executeWhenFinished) {

        mBillingProvider.getBillingManager().querySkuDetailsAsync(billingType, skusList, new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {

                if (responseCode != BillingResponse.OK) {
                    if (DEBUG) logger.w(TAG, "Unsuccessful query for type: "
                            + billingType + ". Error code: " + responseCode);
                } else if (skuDetailsList != null && skuDetailsList.size() > 0) {
                    // If we successfully got SKUs, add a header in front of the row
                    @StringRes int stringRes = (billingType.equals(SkuType.INAPP)) ? R.string.billing_header_inapp : R.string.billing_header_subscriptions;
                    inList.add(new SkuRowData(getString(stringRes)));
                    // Then fill all the other rows
                    for (SkuDetails details : skuDetailsList) {
                        if (DEBUG) logger.i(TAG, "Adding sku: " + details.getSku());
                        inList.add(new SkuRowData(details, SkusAdapter.TYPE_NORMAL, billingType));
                    }

                    if (inList.size() == 0) {
                        displayAnErrorIfNeeded();
                    } else {
                        if (mRecyclerView.getAdapter() == null) {
                            mRecyclerView.setAdapter(mAdapter);
                            Resources res = getContext().getResources();
                            mRecyclerView.addItemDecoration(new CardsWithHeadersDecoration(mAdapter,
                                    (int) res.getDimension(R.dimen.billing_header_gap),
                                    (int) res.getDimension(R.dimen.billing_row_gap)));
                            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
}