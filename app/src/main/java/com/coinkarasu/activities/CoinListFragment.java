package com.coinkarasu.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.coinkarasu.R;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.activities.etc.Section;
import com.coinkarasu.adapters.CoinListAdapter;
import com.coinkarasu.billingmodule.BillingActivity;
import com.coinkarasu.coins.AdCoinImpl;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.services.UpdateToplistIntentService;
import com.coinkarasu.tasks.CollectCoinsTask;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.Tutorial;

import java.util.ArrayList;
import java.util.List;


public class CoinListFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        TimeProvider {

    private static final boolean DEBUG = true;
    private static final String TAG = "CoinListFragment";
    private static final String STATE_IS_VISIBLE_TO_USER_KEY = "isVisibleToUser";

    private NavigationKind kind;
    private boolean isVisibleToUser;
    private boolean isSelected;
    private ProgressBar loadingIndicator;
    private RecyclerView recyclerView;
    private CoinListAdapter adapter;
    private SwipeRefreshLayout refresh;
    private List<CoinListSectionFragment> sectionFragments;

    public CoinListFragment() {
    }

    public static CoinListFragment newInstance(NavigationKind kind, boolean isSelected) {
        CoinListFragment fragment = new CoinListFragment();
        Bundle args = new Bundle();
        args.putString("kind", kind.name());
        args.putBoolean("isSelected", isSelected);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            kind = NavigationKind.valueOf(getArguments().getString("kind"));
            isSelected = getArguments().getBoolean("isSelected");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_list, container, false);

        sectionFragments = new ArrayList<>(kind.sections.size());

        if (savedInstanceState != null) {
            isVisibleToUser = savedInstanceState.getBoolean(STATE_IS_VISIBLE_TO_USER_KEY);

            FragmentManager manager = getChildFragmentManager();
            for (Section section : kind.sections) {
                Fragment fragment = manager.findFragmentByTag(kind + section.toString());
                if (fragment != null) {
                    sectionFragments.add((CoinListSectionFragment) fragment);
                }
            }
        } else {
            isVisibleToUser = isSelected; // タブの追加/削除後に利用している

            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            for (Section section : kind.sections) {
                CoinListSectionFragment fragment = CoinListSectionFragment.newInstance(kind, section);
                sectionFragments.add(fragment);
                transaction.add(fragment, kind + section.toString());
            }
            transaction.commit();
        }

        loadingIndicator = view.findViewById(R.id.screen_wait);
        loadingIndicator.setIndeterminateDrawable(getResources().getDrawable(kind.progressDrawableResId));
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(CoinListAdapter.TYPE_HEADER, 0);

        refresh = view.findViewById(R.id.refresh_layout);
        refresh.setOnRefreshListener(this);
        refresh.setColorSchemeColors(getResources().getColor(R.color.colorRotate));

        return view;
    }

    private void setupAdapter() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        if (adapter != null) {
            if (sectionFragments != null) {
                for (CoinListSectionFragment fragment : sectionFragments) {
                    fragment.onAdapterSetupFinished();
                }
            }
            return;
        }

        List<Coin> coins = new ArrayList<>();
        collectCoins(coins, kind.sections.get(0));
    }

    private void collectCoins(final List<Coin> inCoins, final Section section) {
        CoinListSectionFragment fragment = null;
        for (CoinListSectionFragment f : sectionFragments) {
            if (f.getSection().toString().equals(section.toString())) {
                fragment = f;
                break;
            }
        }

        fragment.collectCoins(new CollectCoinsTask.Listener() {
            @Override
            public void coinsCollected(List<Coin> coins) {
                inCoins.addAll(coins);
                int indexOfSection = kind.sections.indexOf(section);

                if (indexOfSection < kind.sections.size() - 1) {
                    collectCoins(inCoins, kind.sections.get(indexOfSection + 1));
                } else {
                    if (!((MainActivity) getActivity()).isPremiumPurchased() && kind.isToplist()
                            && section.getExchange() == Exchange.cccagg && inCoins.size() >= 3) {
                        inCoins.add(2, new AdCoinImpl());
                    }

                    if (adapter == null) {
                        adapter = new CoinListAdapter(getActivity(), CoinListFragment.this, kind, inCoins);
                        initializeRecyclerView();
                    }
                }
            }
        });
    }

    private void initializeRecyclerView() {
        if (getActivity() == null || getActivity().isFinishing() || isDetached() || !isAdded()) {
            return;
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        ((DefaultItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        adapter.setIsScrolled(false);
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        adapter.setIsScrolled(true);
                        break;
                }
            }
        });

        recyclerView.setAdapter(adapter);
        loadingIndicator.setVisibility(View.GONE);

        for (CoinListSectionFragment fragment : sectionFragments) {
            fragment.onAdapterSetupFinished();
        }

        if (isVisibleToUser && kind == NavigationKind.coincheck) {
            Tutorial.showTabTutorial(getActivity(), recyclerView);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isVisibleToUser && kind != null && kind.isToplist() && getActivity() != null) {
            UpdateToplistIntentService.start(getActivity(), kind);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (sectionFragments != null) {
            for (Fragment fragment : sectionFragments) {
                fragment.onDetach();
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // This method may be called outside of the fragment lifecycle.
        this.isVisibleToUser = isVisibleToUser;

        if (isVisibleToUser) {
            // フラグメントのライフサイクルと結びつかないイベントでオートアップデートを起動する。
            // 例) タブの初期化後に、タブのコンテンツを表示
            setupAdapter();
        }

        if (sectionFragments != null) {
            for (Fragment fragment : sectionFragments) {
                fragment.setUserVisibleHint(isVisibleToUser);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(STATE_IS_VISIBLE_TO_USER_KEY, isVisibleToUser);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        if (key.equals("pref_currency") && isVisibleToUser && getActivity() != null) {
            Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.enter);
            recyclerView.startAnimation(anim);
        }
    }

    @Override
    public void onRefresh() {
        if (DEBUG) CKLog.d(TAG, "onRefresh() " + kind.name());

        refresh.setRefreshing(false);

        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        if (((MainActivity) getActivity()).isPremiumPurchased()) {
            for (CoinListSectionFragment fragment : sectionFragments) {
                fragment.forceRefresh();
            }
        } else {
            BillingActivity.start(getActivity(), R.string.billing_dialog_pull_to_refresh);
        }

    }

    public CoinListAdapter getAdapter() {
        return adapter;
    }

    @Override
    public long getLastUpdated(Section section) {
        long time = -1;
        if (sectionFragments == null) {
            return time;
        }

        for (CoinListSectionFragment fragment : sectionFragments) {
            if (section.equals(fragment.getSection())) {
                time = fragment.getLastUpdated();
                break;
            }
        }
        return time;
    }
}
