package com.example.coinkarasu.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.coinkarasu.R;
import com.example.coinkarasu.activities.etc.CoinKind;
import com.example.coinkarasu.activities.etc.Exchange;
import com.example.coinkarasu.activities.etc.NavigationKind;
import com.example.coinkarasu.adapters.ListViewAdapter;
import com.example.coinkarasu.animator.ValueAnimatorBase;
import com.example.coinkarasu.api.cryptocompare.data.PricesImpl;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.pagers.MainPagerAdapter;
import com.example.coinkarasu.tasks.CollectCoinsTask;
import com.example.coinkarasu.tasks.by_exchange.GetPricesByExchangeTaskBase;
import com.example.coinkarasu.tasks.by_exchange.Price;
import com.example.coinkarasu.utils.AutoUpdateTimer;
import com.example.coinkarasu.utils.PrefHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;


public class ListViewByExchangeFragment extends Fragment implements
        ListView.OnItemClickListener,
        ListView.OnScrollListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        CollectCoinsTask.Listener,
        MainPagerAdapter.Listener,
        GetPricesByExchangeTaskBase.Listener {

    private static final boolean DEBUG = true;
    private static final String STATE_IS_VISIBLE_TO_USER_KEY = "isVisibleToUser";

    private AutoUpdateTimer autoUpdateTimer;
    private NavigationKind kind;
    private boolean isVisibleToUser;
    private boolean isSelected;
    private boolean isStartTaskRequested;
    private boolean isRecreated;

    public ListViewByExchangeFragment() {
    }

    public static ListViewByExchangeFragment newInstance(NavigationKind kind, boolean isSelected) {
        ListViewByExchangeFragment fragment = new ListViewByExchangeFragment();
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
        View view = inflater.inflate(R.layout.fragment_list_view, container, false);

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
                .setFromSymbols(Utils.getFromSymbols(getResources(), kind))
                .setListener(this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void initializeListView(ListViewAdapter adapter) {
        if (adapter == null || getActivity() == null || getView() == null) {
            return;
        }

        ListView listView = getView().findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);
        ViewCompat.setNestedScrollingEnabled(listView, true); // <= 21 http://starzero.hatenablog.com/entry/2015/09/30/114136

//        updateViewIfCacheExist(adapter);

        if (isStartTaskRequested) {
            startTask();
        }
    }

    private void updateViewIfCacheExist(ListViewAdapter adapter) {
        if (isDetached() || getView() == null) {
            return;
        }

        adapter.pauseAnimation();

        String tag = kind.name() + "-" + Exchange.valueOf(kind.name());
        boolean isCacheExists = PricesImpl.isCacheExist(getActivity(), tag);
        if (!isCacheExists) {
            return;
        }

//        Prices prices = PricesImpl.restoreFromCache(getActivity(), tag);
//        String exchange = prices.getExchange();
//        ArrayList<Coin> filtered = adapter.getItems(exchange);
//        prices.copyAttrsToCoins(filtered);

        adapter.notifyDataSetChanged();

        adapter.restartAnimation();
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
        initializeListView(new ListViewAdapter(getActivity(), coins, getChildFragmentManager()));
    }

    private void startTask() {
        if (!isAdded() || getActivity() == null || isDetached() || getView() == null) {
            return;
        }

        ListView listView = getView().findViewById(R.id.list_view);
        ListViewAdapter adapter = (ListViewAdapter) listView.getAdapter();

        if (adapter == null) {
            isStartTaskRequested = true;
            return;
        }

        adapter.setAnimEnabled(PrefHelper.isAnimEnabled(getActivity()));
        adapter.setDownloadIconEnabled(PrefHelper.isDownloadIconEnabled(getActivity()));
        adapter.setToSymbol(Utils.getToSymbol(getActivity(), kind));

        GetPricesByExchangeTaskBase.getInstance(getActivity(), Exchange.coincheck, CoinKind.sales)
                .setListener(this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        GetPricesByExchangeTaskBase.getInstance(getActivity(), Exchange.coincheck, CoinKind.trading)
                .setListener(this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void startAutoUpdate(String caller) {
        if (autoUpdateTimer != null) {
            return;
        }

        if (DEBUG) Log.e("startAutoUpdate", "kind=" + (kind == null ? "null" : kind.name()) + " caller=" + caller);

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
        if (DEBUG) Log.e("stopAutoUpdate", "kind=" + (kind == null ? "null" : kind.name()) + " caller=" + caller);
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
        if (kind == null) {
            return;
        }
        setProgressbarVisibility(View.VISIBLE, exchange, coinKind);
    }

    @Override
    public void finished(Exchange exchange, CoinKind coinKind, ArrayList<Price> prices) {
        if (isDetached() || getActivity() == null) {
            stopAutoUpdate("finished");
            return;
        }

        if (prices == null || prices.isEmpty()) {
            return;
        }

        String tag = getTimerTag(kind);
        if (autoUpdateTimer == null || tag == null || !autoUpdateTimer.getTag().equals(tag)) {
            return;
        }

//        prices.saveToCache(getActivity(), kind.name() + "-" + prices.getExchange());

        ListView listView = getView().findViewById(R.id.list_view);
        ListViewAdapter adapter = (ListViewAdapter) listView.getAdapter();

        ArrayList<Coin> coins = adapter.getItems();
        for (Price price : prices) {
            for (Coin coin : coins) {
                if (coin.getSymbol().equals(price.fromSymbol)) {
                    coin.setPrice(price.value);
                    break;
                }
            }

        }

        adapter.notifyDataSetChanged();
        hideProgressbarDelayed(exchange, coinKind);

        Log.d("UPDATED", kind.name() + ", " + new Date().toString());
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

        ImageView progressbar = getView().findViewWithTag(exchange + "-progressbar");
        if (progressbar == null) {
            return;
        }

        if (flag == View.GONE) {
            progressbar.clearAnimation();
            progressbar.setImageResource(R.drawable.ic_refresh_stop);
            updateRelativeTimeSpanText(exchange, coinKind);
        } else if (flag == View.VISIBLE) {
            progressbar.setImageResource(R.drawable.ic_refresh_rotate);
            Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
            progressbar.startAnimation(anim);
        }
    }

    private void updateRelativeTimeSpanText(Exchange exchange, CoinKind coinKind) {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(RelativeTimeSpanFragment.getTag(exchange, coinKind));
        if (fragment != null) {
            ((RelativeTimeSpanFragment) fragment).updateText(System.currentTimeMillis());
        }
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
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        Coin coin = (Coin) ((ListView) parent).getItemAtPosition(pos);
        if (coin.isSectionHeader()) {
            return;
        }

        stopAutoUpdate("onItemClick");
        Intent intent = new Intent(view.getContext(), CoinActivity.class);
        intent.putExtra(CoinActivity.COIN_NAME_KEY, coin.toJson().toString());
        intent.putExtra(CoinActivity.COIN_SYMBOL_KEY, coin.getSymbol());
        startActivity(intent);
    }

    @Override
    public void onScrollStateChanged(AbsListView listView, int state) {
        switch (state) {
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                ((ListViewAdapter) listView.getAdapter()).setIsScrolled(false);
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                ((ListViewAdapter) listView.getAdapter()).setIsScrolled(true);
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                break;
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
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
            getView().findViewById(R.id.list_view).startAnimation(anim);

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

        Exchange exchange = Exchange.valueOf(kind.name());
        for (CoinKind coinKind : CoinKind.values()) {
            Fragment fragment = manager.findFragmentByTag(RelativeTimeSpanFragment.getTag(exchange, coinKind));
            if (fragment != null) {
                transaction.remove(fragment);
            }
        }

        transaction.commitNowAllowingStateLoss();
    }

    private static class Utils {

        static String[] getFromSymbols(Resources resources, NavigationKind kind) {
            Exchange exchange = Exchange.valueOf(kind.name());
            String[] trading = resources.getStringArray(exchange.tradingSymbolsResId);
            String[] sales = resources.getStringArray(exchange.salesSymbolsResId);

            String[] fromSymbols = new String[trading.length + sales.length];
            System.arraycopy(trading, 0, fromSymbols, 0, trading.length);
            System.arraycopy(sales, 0, fromSymbols, trading.length, sales.length);

            return fromSymbols;
        }

        static String getToSymbol(Activity activity, NavigationKind kind) {
            return "JPY";
        }

        static ArrayList<Coin> insertSectionHeader(ArrayList<Coin> coins, NavigationKind kind) {
            ArrayList<Coin> sectionalCoins = new ArrayList<>();
            Exchange exchange = Exchange.valueOf(kind.name());
            List<Coin> selected;

            sectionalCoins.add(exchange.createSectionHeaderCoin(CoinKind.trading));

            selected = coins.subList(0, 1);
            for (Coin coin : selected) {
                coin.setExchange(exchange.name());
                coin.setCoinKind(CoinKind.trading);
            }

            sectionalCoins.addAll(selected);
            sectionalCoins.add(exchange.createSectionHeaderCoin(CoinKind.sales));

            selected = coins.subList(1, coins.size());
            for (Coin coin : selected) {
                coin.setExchange(exchange.name());
                coin.setCoinKind(CoinKind.sales);
            }

            sectionalCoins.addAll(selected);

            return sectionalCoins;
        }
    }
}
