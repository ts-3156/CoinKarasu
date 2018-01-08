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


public class HomeTabFragment extends Fragment {

    private static final boolean DEBUG = true;
    private static final String STATE_IS_VISIBLE_TO_USER_KEY = "isVisibleToUser";
    public static final String ACTION_UPDATE_TRENDING = "updateTrending";

    private boolean isVisibleToUser;
    private boolean isRecreated;
    private BroadcastReceiver receiver;

    public HomeTabFragment() {
    }

    public static HomeTabFragment newInstance(NavigationKind kind) {
        HomeTabFragment fragment = new HomeTabFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_tab, container, false);

        ((ProgressBar) view.findViewById(R.id.screen_wait))
                .setIndeterminateDrawable(getResources().getDrawable(NavigationKind.home.progressDrawableResId));

        if (savedInstanceState != null) {
            isVisibleToUser = savedInstanceState.getBoolean(STATE_IS_VISIBLE_TO_USER_KEY);
            isRecreated = true;
        } else {
            isVisibleToUser = true; // TODO 最初に選択されているタブはホームタブなので、必要ないかもしれない。
            isRecreated = false;
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isVisibleToUser) {
                    Bundle bundle = intent.getExtras();
                    initializeCards(TrendingKind.valueOf(bundle.getString("kind")));
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver,
                new IntentFilter(ACTION_UPDATE_TRENDING));

        return view;
    }

    private void initializeCards() {
        if (!isAdded() || isDetached()) {
            return;
        }

        // TODO 現状では、タブが表示されるたびにsetUserVisibleHintから初期化している。
        // TODO 効率をより重視するなら、データが更新された場合のみ初期化する方がよい。

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        for (TrendingKind kind : TrendingKind.values()) {
            transaction.replace(kind.containerId, HomeTabCardFragment.newInstance(kind), kind.tag);
        }
        transaction.commitNowAllowingStateLoss();

        getView().findViewById(R.id.screen_wait).setVisibility(View.GONE);
    }

    private void initializeCards(TrendingKind kind) {
        if (!isAdded() || isDetached()) {
            return;
        }

        getChildFragmentManager().beginTransaction()
                .replace(kind.containerId, HomeTabCardFragment.newInstance(kind), kind.tag)
                .commitNowAllowingStateLoss();
    }

    @Override
    public void onResume() {
        super.onResume();

        // フラグメントが破棄されずに再開された場合は、ここでカードを初期化する。
        if (!isRecreated && isVisibleToUser) {
            initializeCards();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (receiver != null) {
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
            initializeCards();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(STATE_IS_VISIBLE_TO_USER_KEY, isVisibleToUser);
        super.onSaveInstanceState(savedInstanceState);
    }
}
