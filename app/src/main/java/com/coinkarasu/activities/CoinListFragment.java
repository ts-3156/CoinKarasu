package com.coinkarasu.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.coinkarasu.R;
import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.activities.etc.Section;
import com.coinkarasu.adapters.CoinListRecyclerViewAdapter;
import com.coinkarasu.animator.ValueAnimatorBase;
import com.coinkarasu.coins.AdCoinImpl;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.data.Toplist;
import com.coinkarasu.pagers.MainPagerAdapter;
import com.coinkarasu.tasks.CollectCoinsTask;
import com.coinkarasu.tasks.by_exchange.GetCccaggPricesTask;
import com.coinkarasu.tasks.by_exchange.GetPricesByExchangeTaskBase;
import com.coinkarasu.tasks.by_exchange.data.CachedPrices;
import com.coinkarasu.tasks.by_exchange.data.Price;
import com.coinkarasu.utils.Log;
import com.coinkarasu.utils.PeriodicalUpdater;
import com.coinkarasu.utils.PrefHelper;

import java.util.ArrayList;
import java.util.Arrays;


public class CoinListFragment extends Fragment implements
        GetPricesByExchangeTaskBase.Listener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        MainPagerAdapter.Listener {

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

        updater = new PeriodicalUpdater(this, kind, PrefHelper.getSyncInterval(getActivity()));
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
                    public void coinsCollected(ArrayList<Coin> coins) {
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
                                adapter = new CoinListRecyclerViewAdapter(getActivity(), inCoins);
                                initializeRecyclerView(recyclerView, (CoinListRecyclerViewAdapter) adapter);
                            }
                        }
                    }
                })
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void initializeRecyclerView(RecyclerView recyclerView, CoinListRecyclerViewAdapter adapter) {
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
                        ((CoinListRecyclerViewAdapter) recyclerView.getAdapter()).setIsScrolled(false);
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        ((CoinListRecyclerViewAdapter) recyclerView.getAdapter()).setIsScrolled(true);
                        break;
                }
            }
        });

        adapter.setOnItemClickListener(new CoinListRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Coin coin, View view, int position) {
                if (coin.isSectionHeader() || coin.isAdCoin()) {
                    return;
                }

                if (updater != null) {
                    updater.stop("onItemClick");
                }
                Intent intent = new Intent(view.getContext(), CoinActivity.class);
                intent.putExtra(CoinActivity.KEY_COIN_JSON, coin.toJson().toString());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        updateViewIfCacheExist(adapter);

        if (isStartTaskRequested) {
            startTask();
        }
    }

    private void updateViewIfCacheExist(Exchange exchange, CoinKind coinKind, CoinListRecyclerViewAdapter adapter) {
        if (!CachedPrices.isCacheExist(getActivity(), kind, exchange, coinKind)) {
            return;
        }

        CachedPrices cachedPrices = CachedPrices.restoreFromCache(getActivity(), kind, exchange, coinKind);
        if (cachedPrices == null || cachedPrices.getPrices() == null || cachedPrices.getPrices().isEmpty()) {
            return;
        }

        ArrayList<Coin> coins = adapter.getItems(exchange, coinKind);
        if (coins.isEmpty()) {
            return;
        }

        for (Price price : cachedPrices.getPrices()) {
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

    private void updateViewIfCacheExist(CoinListRecyclerViewAdapter adapter) {
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
        CoinListRecyclerViewAdapter adapter = (CoinListRecyclerViewAdapter) recyclerView.getAdapter();

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
        setProgressbarVisibility(View.VISIBLE, exchange, coinKind);
    }

    @Override
    public void finished(Exchange exchange, CoinKind coinKind, ArrayList<Price> prices) {
        if (isDetached() || getActivity() == null) {
            if (updater != null) {
                updater.stop("finished");
            }
            return;
        }

        if (prices == null) {
            return;
        }

        new CachedPrices(prices).saveToCache(getActivity(), kind, exchange, coinKind);

        RecyclerView recyclerView = getView().findViewById(R.id.recycler_view);
        CoinListRecyclerViewAdapter adapter = (CoinListRecyclerViewAdapter) recyclerView.getAdapter();

        ArrayList<Coin> coins = adapter.getItems(exchange, coinKind);

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

    private void hideProgressbarDelayed(final Exchange exchange, final CoinKind coinKind) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setProgressbarVisibility(View.GONE, exchange, coinKind);
            }
        }, ValueAnimatorBase.DURATION);
    }

    private void setProgressbarVisibility(int flag, Exchange exchange, CoinKind coinKind) {
        if (isDetached() || getView() == null) {
            return;
        }

        ImageView progressbar = getView().findViewWithTag(exchange.name() + "-" + coinKind.name() + "-progressbar");
        if (progressbar == null) {
            return;
        }

        if (flag == View.GONE) {
            progressbar.clearAnimation();
            progressbar.setImageResource(R.drawable.ic_refresh_stop);
        } else if (flag == View.VISIBLE) {
            progressbar.setImageResource(R.drawable.ic_refresh_rotate);
            Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
            progressbar.startAnimation(anim);
        }
    }

    private void updateRelativeTimeSpanText(Exchange exchange, CoinKind coinKind) {
        String tag = RelativeTimeSpanFragment.getTag(exchange, coinKind);
        Fragment fragment = getChildFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            View timeSpanContainer = getView().findViewWithTag(exchange.name() + "-" + coinKind.name() + "-time_span");
            if (timeSpanContainer == null) {
                return;
            }
            timeSpanContainer.setId(exchange.getHeaderNameResId(coinKind)); // 何かしらの値をセットしないと、すべて同じIDになってしまう。

            fragment = RelativeTimeSpanFragment.newInstance(System.currentTimeMillis());
            getChildFragmentManager().beginTransaction()
                    .replace(timeSpanContainer.getId(), fragment, tag)
                    .commit();
        }
        ((RelativeTimeSpanFragment) fragment).updateText(System.currentTimeMillis());
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

    @Override
    public void removeAllNestedFragments() {
        if (!isAdded() || isDetached()) {
            return;
        }

        FragmentManager manager = getChildFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        for (Exchange exchange : kind.exchanges) {
            for (CoinKind coinKind : CoinKind.values()) {
                Fragment fragment = manager.findFragmentByTag(RelativeTimeSpanFragment.getTag(exchange, coinKind));
                if (fragment != null) {
                    transaction.remove(fragment);
                }
            }
        }

        transaction.commitNowAllowingStateLoss();
    }
}
