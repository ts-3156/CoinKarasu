package com.coinkarasu.activities;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.coinkarasu.R;
import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.activities.etc.Section;
import com.coinkarasu.adapters.CoinListAdapter;
import com.coinkarasu.animator.ValueAnimatorBase;
import com.coinkarasu.coins.AdCoinImpl;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.custom.AggressiveProgressbar;
import com.coinkarasu.custom.RelativeTimeSpanTextView;
import com.coinkarasu.data.Toplist;
import com.coinkarasu.pagers.MainPagerAdapter;
import com.coinkarasu.tasks.CollectCoinsTask;
import com.coinkarasu.tasks.by_exchange.GetCccaggPricesTask;
import com.coinkarasu.tasks.by_exchange.GetPricesByExchangeTaskBase;
import com.coinkarasu.tasks.by_exchange.data.PricesCache;
import com.coinkarasu.tasks.by_exchange.data.Price;
import com.coinkarasu.utils.Log;
import com.coinkarasu.utils.PeriodicalUpdater;
import com.coinkarasu.utils.PrefHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CoinListFragment extends Fragment implements
        GetPricesByExchangeTaskBase.Listener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        MainPagerAdapter.Listener,
        PeriodicalUpdater.PeriodicallyRunnable {

    private static final boolean DEBUG = true;
    private static final String TAG = "CoinListFragment";
    private static final String STATE_IS_VISIBLE_TO_USER_KEY = "isVisibleToUser";

    private PeriodicalUpdater updater;
    private NavigationKind kind;
    private boolean isVisibleToUser;
    private boolean isSelected;
    private boolean isStartTaskRequested;
    private boolean isRecreated;
    private Log logger;

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
        logger = new Log(getActivity());

        if (savedInstanceState != null) {
            isVisibleToUser = savedInstanceState.getBoolean(STATE_IS_VISIBLE_TO_USER_KEY);
            isRecreated = true;
        } else {
            isVisibleToUser = isSelected; // TODO 最初に選択されているタブはホームタブなので、必要ないかもしれない。
            isRecreated = false;
        }

        updater = new PeriodicalUpdater(this, PrefHelper.getSyncInterval(getActivity()));
        isStartTaskRequested = false;
        PrefHelper.getPref(getActivity()).registerOnSharedPreferenceChangeListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // アクティビティの再作成後は、ページャーによってすぐに次の新しいインスタンスが作成される。
        // その場合は、ここでは何もしない。そうすることで、後続の処理はすべて実行されなくなる。
        if (isRecreated) {
            return;
        }

        collectCoins();
    }

    private void collectCoins() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        ArrayList<Coin> coins = new ArrayList<>();
        addCoins(coins, kind.sections[0]);
    }

    private void addCoins(final ArrayList<Coin> inCoins, final Section section) {
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

                        if (kind.isToplist() && section.getExchange() == Exchange.cccagg && inCoins.size() >= 3) {
                            inCoins.add(2, new AdCoinImpl());
                        }

                        int indexOfSection = Arrays.asList(kind.sections).indexOf(section);
                        if (indexOfSection == -1) {
                            throw new RuntimeException("Invalid section " + section.toString());
                        }

                        if (indexOfSection < kind.sections.length - 1) {
                            addCoins(inCoins, kind.sections[indexOfSection + 1]);
                        } else {
                            RecyclerView recyclerView = getView().findViewById(R.id.recycler_view);
                            RecyclerView.Adapter adapter = recyclerView.getAdapter();
                            if (adapter == null) {
                                adapter = new CoinListAdapter(getContext(), inCoins);
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
        adapter.restartAnimation();
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
    public void finished(Exchange exchange, CoinKind coinKind, ArrayList<Price> prices) {
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
        CoinListAdapter adapter = (CoinListAdapter) recyclerView.getAdapter();

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

        adapter.notifyCoinsChanged(exchange, coinKind);
        updateRelativeTimeSpanText(exchange, coinKind);
        hideProgressbarDelayed(exchange, coinKind);

        if (DEBUG) logger.d(TAG, "finished() " + exchange + ", " + coinKind);
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

    private void updateRelativeTimeSpanText(Exchange exchange, CoinKind coinKind) {
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
        if (!isRecreated && isVisibleToUser && updater != null) {
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
}
