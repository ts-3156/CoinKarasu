package com.coinkarasu.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.activities.etc.Section;
import com.coinkarasu.adapters.CoinListAdapter;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.custom.AggressiveProgressbar;
import com.coinkarasu.custom.RelativeTimeSpanTextView;
import com.coinkarasu.tasks.CollectCoinsTask;
import com.coinkarasu.tasks.by_exchange.GetPricesByExchangeTaskBase;
import com.coinkarasu.tasks.by_exchange.GetToplistPricesTask;
import com.coinkarasu.tasks.by_exchange.data.Price;
import com.coinkarasu.tasks.by_exchange.data.PricesCache;
import com.coinkarasu.utils.CKDateUtils;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.PeriodicalUpdater;
import com.coinkarasu.utils.PrefHelper;

import java.util.ArrayList;
import java.util.List;


public class CoinListSectionFragment extends Fragment implements
        GetPricesByExchangeTaskBase.Listener,
        PeriodicalUpdater.PeriodicalTask {

    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "CoinListSectionFragment";
    private static final String STATE_IS_VISIBLE_TO_USER_KEY = "isVisibleToUser";
    private static final String STATE_LAST_UPDATED_KEY = "lastUpdated";

    private PeriodicalUpdater updater;
    private boolean isVisibleToUser;

    private CoinListFragment parent;
    private NavigationKind kind;
    private Section section;
    private List<Coin> coins;
    private AggressiveProgressbar progressbar;

    public CoinListSectionFragment() {
    }

    public static CoinListSectionFragment newInstance(NavigationKind kind, Section section) {
        CoinListSectionFragment fragment = new CoinListSectionFragment();
        Bundle args = new Bundle();
        args.putString("kind", kind.name());
        args.putSerializable("section", section);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            kind = NavigationKind.valueOf(getArguments().getString("kind"));
            section = (Section) getArguments().getSerializable("section");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        updater = new PeriodicalUpdater(this, PrefHelper.getSyncInterval(getActivity()));

        parent = (CoinListFragment) getParentFragment();

        if (savedInstanceState != null) {
            isVisibleToUser = savedInstanceState.getBoolean(STATE_IS_VISIBLE_TO_USER_KEY);
            updater.setLastUpdated(savedInstanceState.getLong(STATE_LAST_UPDATED_KEY), false);
            if (DEBUG) CKLog.d(TAG, "lastUpdated is restored "
                    + kind.name() + " " + section.toString() + " " + updater.getLastUpdated());
        }

        return null;
    }

    public synchronized void collectCoins(final CollectCoinsTask.Listener executeOnSuccess) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        if (coins != null && !coins.isEmpty()) {
            if (executeOnSuccess != null) {
                executeOnSuccess.coinsCollected(coins);
            }
            return;
        }

        CKLog.time(TAG);

        CollectCoinsTask.Listener callback = new CollectCoinsTask.Listener() {
            @Override
            public void coinsCollected(List<Coin> inCoins) {
                if (getActivity() == null || getActivity().isFinishing() || isDetached() || !isAdded()) {
                    return;
                }

                if (coins != null && !coins.isEmpty()) {
                    if (executeOnSuccess != null) {
                        executeOnSuccess.coinsCollected(coins);
                    }
                    return;
                }

                String toSymbol = kind.getToSymbol();
                for (Coin coin : inCoins) {
                    coin.setToSymbol(toSymbol);
                    coin.setExchange(section.getExchange().name());
                    coin.setCoinKind(section.getCoinKind());
                }

                updateCoinsIfCacheExists(section, inCoins);

                inCoins.add(0, section.createSectionHeaderCoin());

                coins = new ArrayList<>();
                coins.addAll(inCoins);

                if (DEBUG) CKLog.d(TAG, "collectCoins() "
                        + kind.name() + " " + section.toString() + " " + CKLog.timeEnd(TAG));

                if (executeOnSuccess != null) {
                    executeOnSuccess.coinsCollected(coins);
                }
            }
        };

        new CollectCoinsTask(getActivity(), kind, section)
                .setListener(callback)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void updateCoinsIfCacheExists(Section section, List<Coin> coins) {
        Exchange exchange = section.getExchange();
        CoinKind coinKind = section.getCoinKind();
        List<Price> prices = new PricesCache(getActivity()).get(kind, exchange, coinKind);

        if (prices == null || prices.isEmpty()) {
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

        if (DEBUG) CKLog.d(TAG, "updateCoinsIfCacheExists() " + section.toString());
    }

    public void startUpdating() {
        if (DEBUG) CKLog.d(TAG, "startUpdating() " + section.toString());
        if (getActivity() == null || getActivity().isFinishing() || isDetached() || !isAdded()) {
            if (DEBUG) CKLog.w(TAG, "startUpdating() not initialized or be finishing");
            return;
        }

        if (parent == null || parent.getAdapter() == null) {
            if (DEBUG) CKLog.w(TAG, "startUpdating() parent or adapter is null");
            return;
        }

        CoinListAdapter adapter = parent.getAdapter();
        String toSymbol = kind.getToSymbol();
        adapter.setToSymbol(toSymbol);

        if (PrefHelper.isAirplaneModeOn(getActivity())) {
            updater.setLastUpdated(CKDateUtils.now(), true);
            for (final Section section : kind.sections) {
                new Handler(getActivity().getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshRelativeTime(section.getExchange(), section.getCoinKind(), true);
                        hideProgressbarWithAirplaneMode(section.getExchange(), section.getCoinKind());
                    }
                }, 1000);
            }
            return;
        }

        if (kind.isToplist()) {
            new GetToplistPricesTask(getContext(), kind)
                    .setToSymbol(toSymbol)
                    .setListener(this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            GetPricesByExchangeTaskBase.getInstance(getActivity(), section.getExchange(), section.getCoinKind())
                    .setListener(this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void started(Exchange exchange, CoinKind coinKind) {
        if (DEBUG) CKLog.d(TAG, "started() " + section.toString());
        showProgressbar(exchange, coinKind);
        refreshRelativeTime(exchange, coinKind, false);
    }

    @Override
    public void finished(final Exchange exchange, final CoinKind coinKind, final List<Price> prices, final boolean withWarning) {
        if (isDetached() || getActivity() == null || getActivity().isFinishing()) {
            if (updater != null) {
                updater.stop("finished() " + section.toString());
            }
            return;
        }

        if (prices == null || prices.isEmpty()) {
            if (DEBUG) CKLog.w(TAG, "finished() prices is null " + exchange + " " + coinKind);
            updater.setLastUpdated(CKDateUtils.now(), true);
            refreshRelativeTime(exchange, coinKind, true);
            hideProgressbarWithError(exchange, coinKind);
            return;
        }

        new PricesCache(getContext()).put(kind, exchange, coinKind, prices);

        for (Price price : prices) {
            for (Coin coin : coins) {
                if (coin.isSectionHeader()) {
                    continue;
                }

                if (coin.getSymbol().equals(price.fromSymbol)) {
                    coin.setPrice(price.price);
                    coin.setPriceDiff(price.priceDiff);
                    coin.setTrend(price.trend);
                    break;
                }
            }
        }

        CoinListAdapter adapter = parent.getAdapter();
        adapter.startAnimation(section);
        adapter.notifyCoinsChanged(exchange, coinKind);

        updater.setLastUpdated(CKDateUtils.now(), true);
        refreshRelativeTime(exchange, coinKind, true);
        hideProgressbarDelayed(exchange, coinKind, withWarning);

        if (DEBUG) CKLog.d(TAG, "finished() " + kind + " " + exchange + " " + coinKind);
    }

    private void findProgressbarBySection(Section section) {
        if (parent != null && parent.getView() != null) {
            progressbar = parent.getView().findViewWithTag(makeProgressbarTag(section.getExchange(), section.getCoinKind()));
        }
    }

    private String makeProgressbarTag(Exchange exchange, CoinKind coinKind) {
        return exchange.name() + "-" + coinKind.name() + "-progressbar";
    }

    private void hideProgressbarDelayed(Exchange exchange, CoinKind coinKind, boolean withWarning) {
        findProgressbarBySection(section);
        if (progressbar != null) {
            progressbar.stopAnimationDelayed(withWarning);
        }
    }

    private void hideProgressbarWithAirplaneMode(Exchange exchange, CoinKind coinKind) {
        findProgressbarBySection(section);
        if (progressbar != null) {
            progressbar.stopAnimationWithAirplaneMode();
        }
    }

    private void hideProgressbarWithError(Exchange exchange, CoinKind coinKind) {
        findProgressbarBySection(section);
        if (progressbar != null) {
            progressbar.stopAnimationWithError();
        }
    }

    private void showProgressbar(Exchange exchange, CoinKind coinKind) {
        findProgressbarBySection(section);
        if (progressbar != null) {
            progressbar.startAnimation();
        }
    }

    private void refreshRelativeTime(Exchange exchange, CoinKind coinKind, boolean restart) {
        if (parent == null || parent.getView() == null) {
            return;
        }
        RelativeTimeSpanTextView timeSpan = parent.getView().findViewWithTag(exchange.name() + "-" + coinKind.name() + "-time_span");
        if (timeSpan != null) {
            timeSpan.updateText(restart);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // フラグメントが破棄されずに再開された場合は、ここでオートアップデートを起動する。
        if (isVisibleToUser && updater != null && parent != null && parent.getAdapter() != null) {
            updater.setInterval(PrefHelper.getSyncInterval(getActivity()));
            updater.start("onResume() " + section.toString());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (updater != null) {
            updater.stop("onPause() " + section.toString());
        }
    }

    // viewにアタッチしていないため、CoinListFragmentから直接呼んでいる
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;

        if (isVisibleToUser) {
            // 並行して初期化することによるレースコンディションでのバグを避けるため、
            // 初期化についてはCoinListFragmentで制御している。
        } else {
            // フラグメントのライフサイクルと結びつかないイベントでオートアップデートを停止する。
            // 例) タブの表示後に、他のタブのコンテンツを表示
            if (parent != null && coins != null && !coins.isEmpty() && updater != null) {
                updater.stop("setUserVisibleHint() " + section.toString());
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putBoolean(STATE_IS_VISIBLE_TO_USER_KEY, isVisibleToUser);
        if (updater != null) {
            savedInstanceState.putLong(STATE_LAST_UPDATED_KEY, updater.getLastUpdated());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    public long getLastUpdated() {
        if (updater != null) {
            return updater.getLastUpdated();
        } else {
            return -1L;
        }
    }

    public Section getSection() {
        return section;
    }

    // Adapterの初期化が完了しているということは、collectCoinsも完了している
    public void onAdapterSetupFinished() {
        if (DEBUG) CKLog.d(TAG, "onAdapterSetupFinished() " + section.toString());
        if (updater != null) {
            updater.start("onAdapterSetupFinished() " + section.toString());
        }
    }

    public void forceRefresh() {
        if (updater != null) {
            updater.forceStart("forceRefresh() " + section.toString());
        }
    }
}
