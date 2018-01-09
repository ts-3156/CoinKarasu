package com.coinkarasu.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.coinkarasu.R;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.activities.etc.TrendingKind;
import com.coinkarasu.services.UpdateTrendingIntentService;
import com.coinkarasu.utils.CKLog;


public class HomeTabFragment extends Fragment {

    private static final boolean DEBUG = true;
    private static final String TAG = "HomeTabFragment";
    private static final String STATE_IS_VISIBLE_TO_USER_KEY = "isVisibleToUser";
    public static final String ACTION_UPDATE_TRENDING = "updateTrending";

    private boolean isSelected;
    private boolean isVisibleToUser;
    private BroadcastReceiver receiver;

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

        ((ProgressBar) view.findViewById(R.id.screen_wait))
                .setIndeterminateDrawable(getResources().getDrawable(NavigationKind.home.progressDrawableResId));

        if (savedInstanceState != null) {
            isVisibleToUser = savedInstanceState.getBoolean(STATE_IS_VISIBLE_TO_USER_KEY);
        } else {
            isVisibleToUser = isSelected; // タブの追加/削除後に利用している
        }

        if (isVisibleToUser) {
            initializeCards(view);
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isVisibleToUser) {
                    Bundle bundle = intent.getExtras();
                    refreshCard(TrendingKind.valueOf(bundle.getString("kind")));
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(receiver, new IntentFilter(ACTION_UPDATE_TRENDING));

        UpdateTrendingIntentService.start(getActivity());

        return view;
    }

    private void initializeCards(View view) {
        if (view == null || !isAdded() || isDetached()) {
            return;
        }

        view.findViewById(R.id.screen_wait).setVisibility(View.GONE);

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
    public void onDetach() {
        super.onDetach();
        if (receiver != null && getActivity() != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        // This method may be called outside of the fragment lifecycle.
        this.isVisibleToUser = isVisibleToUser;

        if (isVisibleToUser) {
            // フラグメントのライフサイクルと結びつかないイベントで初期化する。
            // 例) タブの初期化後に、タブのコンテンツを表示
            initializeCards(getView());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(STATE_IS_VISIBLE_TO_USER_KEY, isVisibleToUser);
        super.onSaveInstanceState(savedInstanceState);
    }
}
