package com.coinkarasu.activities;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.activities.etc.Section;
import com.coinkarasu.adapters.CoinListAdapter;
import com.coinkarasu.animator.ValueAnimatorBase;
import com.coinkarasu.billingmodule.BillingActivity;
import com.coinkarasu.coins.AdCoinImpl;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.custom.AggressiveProgressbar;
import com.coinkarasu.custom.RelativeTimeSpanTextView;
import com.coinkarasu.services.data.Toplist;
import com.coinkarasu.tasks.CollectCoinsTask;
import com.coinkarasu.tasks.by_exchange.GetCccaggPricesTask;
import com.coinkarasu.tasks.by_exchange.GetPricesByExchangeTaskBase;
import com.coinkarasu.tasks.by_exchange.data.Price;
import com.coinkarasu.tasks.by_exchange.data.PricesCache;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.PeriodicalUpdater;
import com.coinkarasu.utils.PrefHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CoinListFragment extends Fragment implements
        GetPricesByExchangeTaskBase.Listener,
        SwipeRefreshLayout.OnRefreshListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        PeriodicalUpdater.PeriodicallyRunnable,
        TimeProvider {

    private static final boolean DEBUG = true;
    private static final String TAG = "CoinListFragment";
    private static final String STATE_IS_VISIBLE_TO_USER_KEY = "isVisibleToUser";
    private static final String STATE_LAST_UPDATED_KEY = "lastUpdated";

    private PeriodicalUpdater updater;
    private NavigationKind kind;
    private boolean isVisibleToUser;
    private boolean isSelected;
    private boolean isStartTaskRequested;
    private boolean isCollectCoinsRequested;

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

        isCollectCoinsRequested = false;
        isStartTaskRequested = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_list, container, false);

        ((ProgressBar) view.findViewById(R.id.screen_wait)).setIndeterminateDrawable(getResources().getDrawable(kind.progressDrawableResId));
        updater = new PeriodicalUpdater(this, PrefHelper.getSyncInterval(getActivity()));

        SwipeRefreshLayout refresh = view.findViewById(R.id.refresh_layout);
        refresh.setOnRefreshListener(this);
        refresh.setColorSchemeColors(getResources().getColor(R.color.colorRotate));

        if (savedInstanceState != null) {
            isVisibleToUser = savedInstanceState.getBoolean(STATE_IS_VISIBLE_TO_USER_KEY);
            updater.setLastUpdated(savedInstanceState.getLong(STATE_LAST_UPDATED_KEY));
            if (DEBUG) CKLog.d(TAG, "lastUpdated is restored "
                    + kind.name() + " " + updater.getLastUpdated());
        } else {
            isVisibleToUser = isSelected; // タブの追加/削除後に利用している
        }

        PrefHelper.getPref(getActivity()).registerOnSharedPreferenceChangeListener(this);

        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // collectCoinsはsetUserVisibleHintから呼ばれるが、fragmentの
        // ライフサイクル外から呼ばれることも考慮して、念のためにここでも呼んでいる。
        if (isCollectCoinsRequested) {
            if (DEBUG) CKLog.e(TAG, "onActivityCreated() call collectCoins()");
            collectCoins();
        }
    }

    private void collectCoins() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        if (getView() == null) {
            isCollectCoinsRequested = true;
            return;
        }

        RecyclerView recyclerView = getView().findViewById(R.id.recycler_view);
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter != null) {
            return;
        }

        List<Coin> coins = new ArrayList<>();
        addCoins(coins, kind.sections[0]);
    }

    private void addCoins(final List<Coin> inCoins, final Section section) {
        final long start = System.currentTimeMillis();
        String[] fromSymbols = null;

        if (kind.isToplist()) {
            Toplist toplist = Toplist.restoreFromCache(getActivity(), kind);
            if (toplist != null) {
                fromSymbols = toplist.getSymbols();
            }

            if (fromSymbols == null || fromSymbols.length == 0) {
                fromSymbols = getResources().getStringArray(kind.symbolsResId);
            }
        } else {
            fromSymbols = getResources().getStringArray(section.getSymbolsResId());
        }

        new CollectCoinsTask(getActivity())
                .setFromSymbols(fromSymbols)
                .setListener(new CollectCoinsTask.Listener() {
                    @Override
                    public void coinsCollected(List<Coin> coins) {
                        String toSymbol = kind.getToSymbol();
                        for (Coin coin : coins) {
                            coin.setToSymbol(toSymbol);
                            coin.setExchange(section.getExchange().name());
                            coin.setCoinKind(section.getCoinKind());
                        }

                        inCoins.add(section.createSectionHeaderCoin());
                        inCoins.addAll(coins);

                        if (!PrefHelper.isPremium(getActivity()) && kind.isToplist()
                                && section.getExchange() == Exchange.cccagg && inCoins.size() >= 3) {
                            inCoins.add(2, new AdCoinImpl());
                        }

                        int indexOfSection = Arrays.asList(kind.sections).indexOf(section);
                        if (indexOfSection == -1) {
                            throw new RuntimeException("Invalid section " + section.toString());
                        }

                        if (DEBUG) CKLog.d(TAG, "addCoins() "
                                + kind.name() + " " + (System.currentTimeMillis() - start) + " ms");

                        if (indexOfSection < kind.sections.length - 1) {
                            addCoins(inCoins, kind.sections[indexOfSection + 1]);
                        } else {
                            RecyclerView recyclerView = getView().findViewById(R.id.recycler_view);
                            RecyclerView.Adapter adapter = recyclerView.getAdapter();
                            if (adapter == null) {
                                adapter = new CoinListAdapter(getContext(), CoinListFragment.this, kind, inCoins);
                                initializeRecyclerView(recyclerView, (CoinListAdapter) adapter);
                            }
                        }
                    }
                })
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void initializeRecyclerView(RecyclerView recyclerView, CoinListAdapter adapter) {
        if (getActivity() == null || getView() == null) {
            return;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        ((DefaultItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        ((CoinListAdapter) recyclerView.getAdapter()).setIsScrolled(false);
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        ((CoinListAdapter) recyclerView.getAdapter()).setIsScrolled(true);
                        break;
                }
            }
        });

        recyclerView.setAdapter(adapter);
        getView().findViewById(R.id.screen_wait).setVisibility(View.GONE);

        updateViewIfCacheExist(adapter);

        if (isStartTaskRequested) {
            startTask();
        }
    }

    private void updateViewIfCacheExist(Exchange exchange, CoinKind coinKind, CoinListAdapter adapter) {
        List<Price> prices = new PricesCache(getContext()).get(kind, exchange, coinKind);
        if (prices == null || prices.isEmpty()) {
            return;
        }

        List<Coin> coins = adapter.getItems(exchange, coinKind);
        if (coins.isEmpty()) {
            return;
        }

        for (Price price : prices) {
            for (Coin coin : coins) {
                if (coin.getSymbol().equals(price.fromSymbol)) {
                    coin.setPrice(price.price);
                    coin.setPriceDiff(price.priceDiff);
                    coin.setTrend(price.trend);
                    break;
                }
            }

        }
    }

    private void updateViewIfCacheExist(CoinListAdapter adapter) {
        if (isDetached() || getView() == null) {
            return;
        }

        adapter.pauseAnimation();

        if (kind == NavigationKind.japan) {
            for (Exchange exchange : kind.exchanges) {
                updateViewIfCacheExist(exchange, CoinKind.none, adapter);
            }
        } else if (kind == NavigationKind.coincheck) {
            updateViewIfCacheExist(kind.exchanges[0], CoinKind.trading, adapter);
            updateViewIfCacheExist(kind.exchanges[0], CoinKind.sales, adapter);
        } else {
            updateViewIfCacheExist(kind.exchanges[0], CoinKind.none, adapter);
        }

        adapter.notifyDataSetChanged();
    }

    public void startTask() {
        if (!isAdded() || getActivity() == null || isDetached() || getView() == null) {
            return;
        }

        RecyclerView recyclerView = getView().findViewById(R.id.recycler_view);
        CoinListAdapter adapter = (CoinListAdapter) recyclerView.getAdapter();

        if (adapter == null) {
            isStartTaskRequested = true;
            return;
        }

        String toSymbol = kind.getToSymbol();

        adapter.setAnimEnabled(PrefHelper.isAnimEnabled(getActivity()));
        adapter.setDownloadIconEnabled(PrefHelper.isDownloadIconEnabled(getActivity()));
        adapter.setToSymbol(toSymbol);

        if (kind.isToplist()) {
            String[] fromSymbols = null;

            Toplist toplist = Toplist.restoreFromCache(getActivity(), kind);
            if (toplist != null) {
                fromSymbols = toplist.getSymbols();
            }

            if (fromSymbols == null || fromSymbols.length == 0) {
                fromSymbols = getResources().getStringArray(kind.symbolsResId);
            }

            new GetCccaggPricesTask(getContext(), Exchange.cccagg)
                    .setFromSymbols(fromSymbols)
                    .setToSymbol(toSymbol)
                    .setExchange(kind.exchanges[0].name())
                    .setListener(this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            for (Section section : kind.sections) {
                GetPricesByExchangeTaskBase.getInstance(getActivity(), section.getExchange(), section.getCoinKind())
                        .setListener(this)
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    @Override
    public void started(Exchange exchange, CoinKind coinKind) {
        showProgressbar(exchange, coinKind);
    }

    @Override
    public void finished(final Exchange exchange, final CoinKind coinKind, final List<Price> prices) {
        if (isDetached() || getActivity() == null || getActivity().isFinishing() || getView() == null) {
            if (updater != null) {
                updater.stop("finished");
            }
            return;
        }

        if (prices == null) {
            return;
        }

        new PricesCache(getContext()).put(kind, exchange, coinKind, prices);

        RecyclerView recyclerView = getView().findViewById(R.id.recycler_view);
        final CoinListAdapter adapter = (CoinListAdapter) recyclerView.getAdapter();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null || getActivity().isFinishing()) {
                    return;
                }

                List<Coin> coins = adapter.getItems(exchange, coinKind);

                for (Price price : prices) {
                    for (Coin coin : coins) {
                        if (coin.getSymbol().equals(price.fromSymbol)) {
                            coin.setPrice(price.price);
                            coin.setPriceDiff(price.priceDiff);
                            coin.setTrend(price.trend);
                            break;
                        }
                    }
                }

                updater.setLastUpdated(System.currentTimeMillis());
                adapter.resumeAnimation();
                adapter.notifyCoinsChanged(exchange, coinKind);
                refreshRelativeTime(exchange, coinKind);
                hideProgressbarDelayed(exchange, coinKind);

                if (DEBUG) CKLog.d(TAG, "finished() " + kind + ", " + exchange + ", " + coinKind);
            }
        };

        long delay = adapter.isAnimPaused() ? ValueAnimatorBase.DURATION : 0L;
        new Handler().postDelayed(runnable, delay);
    }

    private void hideProgressbarDelayed(Exchange exchange, CoinKind coinKind) {
        if (getView() == null) {
            return;
        }
        AggressiveProgressbar progressbar = getView().findViewWithTag(exchange.name() + "-" + coinKind.name() + "-progressbar");
        if (progressbar != null) {
            progressbar.stopAnimationDelayed(ValueAnimatorBase.DURATION);
        }
    }

    private void showProgressbar(Exchange exchange, CoinKind coinKind) {
        if (getView() == null) {
            return;
        }
        AggressiveProgressbar progressbar = getView().findViewWithTag(exchange.name() + "-" + coinKind.name() + "-progressbar");
        if (progressbar != null) {
            progressbar.startAnimation();
        }
    }

    private void refreshRelativeTime(Exchange exchange, CoinKind coinKind) {
        if (getView() == null) {
            return;
        }
        View timeSpan = getView().findViewWithTag(exchange.name() + "-" + coinKind.name() + "-time_span");
        if (timeSpan != null) {
            ((RelativeTimeSpanTextView) timeSpan).updateText();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // フラグメントが破棄されずに再開された場合は、ここでオートアップデートを起動する。
        if (isVisibleToUser && updater != null) {
            updater.setInterval(PrefHelper.getSyncInterval(getActivity()));
            updater.start("onResume");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (updater != null) {
            updater.stop("onPause");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        PrefHelper.getPref(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
        updater = null;
        kind = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        // This method may be called outside of the fragment lifecycle.
        this.isVisibleToUser = isVisibleToUser;

        if (isVisibleToUser) {
            // フラグメントのライフサイクルと結びつかないイベントでオートアップデートを起動する。
            // 例) タブの初期化後に、タブのコンテンツを表示
            collectCoins();
            if (updater != null) {
                updater.start("setUserVisibleHint");
            }
        } else {
            // フラグメントのライフサイクルと結びつかないイベントでオートアップデートを停止する。
            // 例) タブの表示後に、他のタブのコンテンツを表示
            if (updater != null) {
                updater.stop("setUserVisibleHint");
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(STATE_IS_VISIBLE_TO_USER_KEY, isVisibleToUser);
        savedInstanceState.putLong(STATE_LAST_UPDATED_KEY, updater.getLastUpdated());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        if (key.equals("pref_currency") && isVisibleToUser && getActivity() != null && getView() != null) {
            Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.enter);
            getView().findViewById(R.id.recycler_view).startAnimation(anim);

            if (isVisibleToUser && updater != null) {
                updater.restart("onSharedPreferenceChanged");
            }
        }
    }

    @Override
    public void onRefresh() {
        if (getView() == null) {
            return;
        }

        CKLog.d(TAG, "onRefresh() " + kind.name());

        SwipeRefreshLayout refresh = getView().findViewById(R.id.refresh_layout);
        refresh.setRefreshing(false);

        if (getActivity() == null) {
            return;
        }

        if (((MainActivity) getActivity()).isPremiumPurchased()) {
            if (updater != null) {
                updater.forceStart("onRefresh");
            }
        } else {
            BillingActivity.start(getActivity(), R.string.billing_dialog_pull_to_refresh);
        }

    }

    @Override
    public long getLastUpdated() {
        if (updater != null) {
            return updater.getLastUpdated();
        } else {
            return -1L;
        }
    }
}
