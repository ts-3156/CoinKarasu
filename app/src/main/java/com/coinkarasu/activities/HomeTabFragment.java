package com.coinkarasu.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.coinkarasu.R;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.activities.etc.TrendingKind;
import com.coinkarasu.billingmodule.BillingActivity;
import com.coinkarasu.services.UpdateTrendingIntentService;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.Tutorial;


public class HomeTabFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final boolean DEBUG = true;
    private static final String TAG = "HomeTabFragment";
    private static final String STATE_IS_VISIBLE_TO_USER_KEY = "isVisibleToUser";
    public static final String ACTION_UPDATE_TRENDING = "updateTrending";

    private boolean isSelected;
    private boolean isVisibleToUser;
    private BroadcastReceiver receiver;
    private SwipeRefreshLayout refresh;
    private ProgressBar progressbar;

    public HomeTabFragment() {
    }

    public static HomeTabFragment newInstance(boolean isSelected) {
        HomeTabFragment fragment = new HomeTabFragment();
        Bundle args = new Bundle();
        args.putBoolean("isSelected", isSelected);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isSelected = getArguments().getBoolean("isSelected");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_tab, container, false);

        progressbar = view.findViewById(R.id.screen_wait);
        progressbar.setIndeterminateDrawable(getResources().getDrawable(NavigationKind.home.progressDrawableResId));

        refresh = view.findViewById(R.id.refresh_layout);
        refresh.setOnRefreshListener(this);
        refresh.setColorSchemeColors(getResources().getColor(R.color.colorRotate));

        if (savedInstanceState != null) {
            isVisibleToUser = savedInstanceState.getBoolean(STATE_IS_VISIBLE_TO_USER_KEY);
        } else {
            isVisibleToUser = isSelected; // タブの追加/削除後に利用している
        }

        if (isVisibleToUser) {
            initializeCards();
        }

        UpdateTrendingIntentService.start(getActivity(), false);

        return view;
    }

    private void initializeCards() {
        if (getActivity() == null || getActivity().isFinishing() || !isAdded() || isDetached()) {
            return;
        }

        if (progressbar != null) {
            progressbar.setVisibility(View.GONE);
        }

        Fragment fragment = getChildFragmentManager().findFragmentByTag(TrendingKind.values()[0].tag);
        if (fragment != null) {
            return;
        }

        if (DEBUG) CKLog.d(TAG, "initializeCards()");

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        for (TrendingKind kind : TrendingKind.values()) {
            transaction.replace(kind.containerId, HomeTabCardFragment.newInstance(kind), kind.tag);
        }
        transaction.commit();

        Tutorial.showTabLayoutTutorial(getActivity(), ((MainActivity) getActivity()).getTabLayout());
    }

    private void refreshCard(TrendingKind kind) {
        if (!isAdded() || isDetached() || getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        if (DEBUG) CKLog.d(TAG, "refreshCard() " + kind.name());

        getChildFragmentManager().beginTransaction()
                .replace(kind.containerId, HomeTabCardFragment.newInstance(kind), kind.tag)
                .commitNowAllowingStateLoss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isVisibleToUser) {
                    refreshCard(TrendingKind.valueOf(intent.getExtras().getString("kind")));
                }
            }
        };
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(receiver, new IntentFilter(ACTION_UPDATE_TRENDING));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (receiver != null && getActivity() != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;

        if (isVisibleToUser) {
            // フラグメントのライフサイクルと結びつかないイベントで初期化する。
            // 例) タブの初期化後に、タブのコンテンツを表示
            initializeCards();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(STATE_IS_VISIBLE_TO_USER_KEY, isVisibleToUser);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRefresh() {
        if (DEBUG) CKLog.d(TAG, "onRefresh() " + NavigationKind.home.name());

        refresh.setRefreshing(false);

        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        if (((MainActivity) getActivity()).isPremium()) {
            UpdateTrendingIntentService.start(getActivity(), true);
        } else {
            BillingActivity.start(getActivity(), R.string.billing_dialog_pull_to_refresh);
        }

    }
}
