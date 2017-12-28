package com.example.coinkarasu.activities;

import android.app.Activity;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.coinkarasu.R;
import com.example.coinkarasu.activities.etc.CoinKind;
import com.example.coinkarasu.activities.etc.Exchange;
import com.example.coinkarasu.activities.etc.NavigationKind;
import com.example.coinkarasu.adapters.CoinListRecyclerViewAdapter;
import com.example.coinkarasu.animator.ValueAnimatorBase;
import com.example.coinkarasu.api.cryptocompare.data.PricesImpl;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.pagers.MainPagerAdapter;
import com.example.coinkarasu.tasks.CollectCoinsTask;
import com.example.coinkarasu.tasks.by_exchange.GetCccaggPricesTask;
import com.example.coinkarasu.tasks.by_exchange.GetPricesByExchangeTaskBase;
import com.example.coinkarasu.tasks.by_exchange.Price;
import com.example.coinkarasu.utils.AutoUpdateTimer;
import com.example.coinkarasu.utils.PrefHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;


public class CoinListFragment extends Fragment implements
        GetPricesByExchangeTaskBase.Listener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        CollectCoinsTask.Listener,
        MainPagerAdapter.Listener {

    private static final boolean DEBUG = true;
    private static final String STATE_IS_VISIBLE_TO_USER_KEY = "isVisibleToUser";

    private AutoUpdateTimer autoUpdateTimer;
    private NavigationKind kind;
    private boolean isVisibleToUser;
    private boolean isSelected;
    private boolean isStartTaskRequested;
    private boolean isRecreated;

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

        if (savedInstanceState != null) {
            isVisibleToUser = savedInstanceState.getBoolean(STATE_IS_VISIBLE_TO_USER_KEY);
            isRecreated = true;
        } else {
            isVisibleToUser = isSelected; // TODO 最初に選択されているタブはホームタブなので、必要ないかもしれない。
            isRecreated = false;
        }

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

        new CollectCoinsTask(getActivity())
                .setFromSymbols(getResources().getStringArray(kind.symbolsResId))
                .setListener(this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void collected(ArrayList<Coin> coins) {
        if (coins != null) {
            String toSymbol = Utils.getToSymbol(getActivity(), kind);
            for (Coin coin : coins) {
                coin.setToSymbol(toSymbol);
            }
        }

        coins = Utils.insertSectionHeader(coins, kind);
        initializeListView(new CoinListRecyclerViewAdapter(getActivity(), coins));
    }

    private void initializeListView(CoinListRecyclerViewAdapter adapter) {
        if (adapter == null || getActivity() == null || getView() == null) {
            return;
        }

        RecyclerView recyclerView = getView().findViewById(R.id.recycler_view);
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
                if (coin.isSectionHeader()) {
                    return;
                }

                stopAutoUpdate("onItemClick");
                Intent intent = new Intent(view.getContext(), CoinActivity.class);
                intent.putExtra(CoinActivity.COIN_NAME_KEY, coin.toJson().toString());
                intent.putExtra(CoinActivity.COIN_SYMBOL_KEY, coin.getSymbol());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

//        updateViewIfCacheExist(adapter);

        if (isStartTaskRequested) {
            startTask();
        }
    }

    private void updateViewIfCacheExist(CoinListRecyclerViewAdapter adapter) {
        if (isDetached() || getView() == null) {
            return;
        }

        adapter.pauseAnimation();

        for (Exchange exchange : kind.exchanges) {
            String tag = kind.name() + "-" + exchange.name();
            boolean isCacheExists = PricesImpl.isCacheExist(getActivity(), tag);
            if (!isCacheExists) {
                continue;
            }

//            Prices prices = PricesImpl.restoreFromCache(getActivity(), tag);
//            ArrayList<Coin> filtered = adapter.getItems(Exchange.valueOf(prices.getExchange()));
//            prices.copyAttrsToCoins(filtered);
//
//            adapter.notifyCoinsChanged();
        }

        adapter.restartAnimation();
    }

    private void startTask() {
        if (!isAdded() || getActivity() == null || isDetached() || getView() == null) {
            return;
        }

        RecyclerView recyclerView = getView().findViewById(R.id.recycler_view);
        CoinListRecyclerViewAdapter adapter = (CoinListRecyclerViewAdapter) recyclerView.getAdapter();

        if (adapter == null) {
            isStartTaskRequested = true;
            return;
        }

        adapter.setAnimEnabled(PrefHelper.isAnimEnabled(getActivity()));
        adapter.setDownloadIconEnabled(PrefHelper.isDownloadIconEnabled(getActivity()));
        adapter.setToSymbol(Utils.getToSymbol(getActivity(), kind));

        if (kind == NavigationKind.japan) {
            for (Exchange exchange : kind.exchanges) {
                if (exchange == Exchange.bitflyer || exchange == Exchange.coincheck) {
                    GetPricesByExchangeTaskBase.getInstance(getActivity(), exchange, CoinKind.none)
                            .setListener(this)
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else if (exchange == Exchange.zaif) {
                    String[] fromSymbols = getResources().getStringArray(exchange.tradingSymbolsResId);

                    new GetCccaggPricesTask(getContext(), exchange)
                            .setFromSymbols(fromSymbols)
                            .setToSymbol(Utils.getToSymbol(getActivity(), kind))
                            .setExchange(exchange.name())
                            .setListener(this)
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        } else if (kind == NavigationKind.coincheck) {
            GetPricesByExchangeTaskBase.getInstance(getActivity(), kind.exchanges[0], CoinKind.trading)
                    .setListener(this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            GetPricesByExchangeTaskBase.getInstance(getActivity(), kind.exchanges[0], CoinKind.sales)
                    .setListener(this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            // jpy_toplist, usd_toplist, btc_toplist and so on
            String[] fromSymbols = getResources().getStringArray(kind.symbolsResId);

            new GetCccaggPricesTask(getContext(), Exchange.cccagg)
                    .setFromSymbols(fromSymbols)
                    .setToSymbol(Utils.getToSymbol(getActivity(), kind))
                    .setExchange(kind.exchanges[0].name())
                    .setListener(this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void startAutoUpdate(String caller) {
        if (autoUpdateTimer != null) {
            return;
        }

        if (DEBUG) Log.e("startAutoUpdate", "kind=" + kind + " caller=" + caller);

        String tag = getTimerTag(kind);
        if (tag == null) {
            return;
        }
        autoUpdateTimer = new AutoUpdateTimer(tag);

        autoUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                startTask();
            }
        }, 0, PrefHelper.getSyncInterval(getActivity()));
    }

    private void stopAutoUpdate(String caller) {
        if (DEBUG) Log.e("stopAutoUpdate", "kind=" + kind + " caller=" + caller);
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
            autoUpdateTimer = null;
        }
    }

    private String getTimerTag(NavigationKind kind) {
        if (kind == null) {
            return null;
        }
        String suffix = Utils.getToSymbol(getActivity(), kind);
        if (suffix == null) {
            return null;
        }
        return kind.name() + "-" + suffix;
    }

    @Override
    public void started(Exchange exchange, CoinKind coinKind) {
        setProgressbarVisibility(View.VISIBLE, exchange, coinKind);
    }

    @Override
    public void finished(Exchange exchange, CoinKind coinKind, ArrayList<Price> prices) {
        if (isDetached() || getActivity() == null) {
            stopAutoUpdate("finished");
            return;
        }

        String tag = getTimerTag(kind);
        if (autoUpdateTimer == null || tag == null || !autoUpdateTimer.getTag().equals(tag)) {
            return;
        }

        if (prices == null) {
            return;
        }

//        prices.saveToCache(getActivity(), kind.name() + "-" + prices.getExchange());

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

        if (DEBUG) Log.e("UPDATED", exchange + ", " + coinKind + ", " + new Date().toString());
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
        Fragment fragment = getChildFragmentManager().findFragmentByTag(RelativeTimeSpanFragment.getTag(exchange, coinKind));
        if (fragment == null) {
            View timeSpan = getView().findViewWithTag(exchange.name() + "-" + coinKind.name() + "-time_span");
            timeSpan.setId(exchange.getHeaderNameResId(coinKind)); // 何かしらの値をセットしないと、すべて同じIDになってしまう。

            fragment = RelativeTimeSpanFragment.newInstance(System.currentTimeMillis());
            getChildFragmentManager().beginTransaction()
                    .replace(timeSpan.getId(), fragment, RelativeTimeSpanFragment.getTag(exchange, coinKind))
                    .commit();
        }
        ((RelativeTimeSpanFragment) fragment).updateText(System.currentTimeMillis());
    }

    @Override
    public void onResume() {
        super.onResume();

        // フラグメントが破棄されずに再開された場合は、ここでオートアップデートを起動する。
        if (!isRecreated && isVisibleToUser) {
            startAutoUpdate("onResume");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoUpdate("onPause");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        PrefHelper.getPref(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
        autoUpdateTimer = null;
        kind = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        // This method may be called outside of the fragment lifecycle.
        this.isVisibleToUser = isVisibleToUser;

        if (isVisibleToUser) {
            // フラグメントのライフサイクルと結びつかないイベントでオートアップデートを起動する。
            // 例) タブの初期化後に、タブのコンテンツを表示
            if (kind != null) {
                startAutoUpdate("setUserVisibleHint");
            }
        } else {
            // フラグメントのライフサイクルと結びつかないイベントでオートアップデートを停止する。
            // 例) タブの表示後に、他のタブのコンテンツを表示
            if (kind != null) {
                stopAutoUpdate("setUserVisibleHint");
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

            if (isVisibleToUser) {
                stopAutoUpdate("onSharedPreferenceChanged");
                startAutoUpdate("onSharedPreferenceChanged");
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

    private static class Utils {
        static String getToSymbol(Activity activity, NavigationKind kind) {
            String symbol;

            if (kind == NavigationKind.japan) {
                symbol = MainActivity.Currency.JPY.name();
            } else {
                if (activity == null) {
                    return null;
                }
                symbol = PrefHelper.getToSymbol(activity);
            }

            return symbol;
        }

        static ArrayList<Coin> insertSectionHeader(ArrayList<Coin> coins, NavigationKind kind) {
            ArrayList<Coin> sectionalCoins = new ArrayList<>();

            if (kind == NavigationKind.japan) {
                for (Exchange exchange : kind.exchanges) {
                    sectionalCoins.add(exchange.createSectionHeaderCoin(CoinKind.none));
                    List<Coin> sub;

                    switch (exchange) {
                        case bitflyer:
                            sub = coins.subList(0, 1);
                            break;
                        case coincheck:
                            sub = coins.subList(1, 2);
                            break;
                        case zaif:
                            sub = coins.subList(2, coins.size());
                            break;
                        default:
                            throw new RuntimeException("Invalid exchange " + exchange.name());
                    }

                    for (Coin coin : sub) {
                        coin.setExchange(exchange.name());
                        coin.setCoinKind(CoinKind.none);
                    }

                    sectionalCoins.addAll(sub);
                }
            } else if (kind == NavigationKind.coincheck) {
                Exchange exchange = kind.exchanges[0];
                List<Coin> sub;

                for (Coin coin : coins) {
                    coin.setExchange(exchange.name());
                }

                sectionalCoins.add(exchange.createSectionHeaderCoin(CoinKind.trading));
                sub = coins.subList(0, 1);
                for (Coin coin : sub) {
                    coin.setCoinKind(CoinKind.trading);
                }
                sectionalCoins.addAll(sub);

                sectionalCoins.add(exchange.createSectionHeaderCoin(CoinKind.sales));
                sub = coins.subList(1, coins.size());
                for (Coin coin : sub) {
                    coin.setCoinKind(CoinKind.sales);
                }
                sectionalCoins.addAll(sub);
            } else if (kind == NavigationKind.jpy_toplist) {
                Exchange exchange = kind.exchanges[0];
                sectionalCoins.add(exchange.createSectionHeaderCoin(CoinKind.none));
                for (Coin coin : coins) {
                    coin.setExchange(exchange.name());
                    coin.setCoinKind(CoinKind.none);
                }
                sectionalCoins.addAll(coins);
            }

            return sectionalCoins;
        }
    }
}
