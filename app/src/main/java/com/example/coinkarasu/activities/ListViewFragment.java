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
import com.example.coinkarasu.adapters.ListViewAdapter;
import com.example.coinkarasu.animator.ValueAnimatorBase;
import com.example.coinkarasu.api.cryptocompare.ClientFactory;
import com.example.coinkarasu.api.cryptocompare.data.Prices;
import com.example.coinkarasu.api.cryptocompare.data.PricesImpl;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.coins.CoinImpl;
import com.example.coinkarasu.coins.SectionHeaderCoinImpl;
import com.example.coinkarasu.pagers.MainPagerAdapter;
import com.example.coinkarasu.tasks.CollectCoinsTask;
import com.example.coinkarasu.tasks.GetPricesTask;
import com.example.coinkarasu.utils.AutoUpdateTimer;
import com.example.coinkarasu.utils.PrefHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;


public class ListViewFragment extends Fragment implements
        ListView.OnItemClickListener,
        ListView.OnScrollListener,
        GetPricesTask.Listener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        CollectCoinsTask.Listener,
        MainPagerAdapter.Listener {

    public enum Exchange {
        bitflyer(R.array.bitflyer_trading_symbols, R.array.bitflyer_sales_symbols, R.string.header_name_bitflyer, R.string.trading_header_name_bitflyer, R.string.sales_header_name_bitflyer),
        coincheck(R.array.coincheck_trading_symbols, R.array.coincheck_sales_symbols, R.string.header_name_coincheck, R.string.trading_header_name_coincheck, R.string.sales_header_name_coincheck),
        zaif(R.array.zaif_trading_symbols, R.array.zaif_sales_symbols, R.string.header_name_zaif, R.string.trading_header_name_zaif, R.string.sales_header_name_zaif),
        cccagg(-1, -1, R.string.header_name_cccagg, R.string.trading_header_name_cccagg, R.string.sales_header_name_cccagg);

        public int tradingSymbolsResId;
        public int salesSymbolsResId;
        int headerNameResId;
        int tradingHeaderNameResId;
        int salesHeaderNameResId;

        Exchange(int tradingSymbolsResId, int salesSymbolsResId, int headerNameResId, int tradingHeaderNameResId, int salesHeaderNameResId) {
            this.tradingSymbolsResId = tradingSymbolsResId;
            this.salesSymbolsResId = salesSymbolsResId;
            this.headerNameResId = headerNameResId;
            this.tradingHeaderNameResId = tradingHeaderNameResId;
            this.salesHeaderNameResId = salesHeaderNameResId;
        }

        public int getHeaderNameResId(CoinImpl.Kind kind) {
            int id;
            switch (kind) {
                case trading:
                    id = tradingHeaderNameResId;
                    break;
                case sales:
                    id = salesHeaderNameResId;
                    break;
                default:
                    id = headerNameResId;
            }
            return id;
        }

        public Coin createSectionHeaderCoin(CoinImpl.Kind kind) {
            return new SectionHeaderCoinImpl(this, kind);
        }
    }

    private static final String STATE_IS_VISIBLE_TO_USER_KEY = "isVisibleToUser";

    private AutoUpdateTimer autoUpdateTimer;
    private NavigationKind kind;
    private boolean isVisibleToUser;
    private boolean isSelected;
    private boolean isStartTaskRequested;

    public ListViewFragment() {
    }

    public static ListViewFragment newInstance(NavigationKind kind, boolean isSelected) {
        ListViewFragment fragment = new ListViewFragment();
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
        } else {
            isVisibleToUser = isSelected;
        }

        isStartTaskRequested = false;
        PrefHelper.getPref(getActivity()).registerOnSharedPreferenceChangeListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

        updateViewIfCacheExist(adapter);

        if (isStartTaskRequested) {
            startTask();
        }
    }

    private void updateViewIfCacheExist(ListViewAdapter adapter) {
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

            Prices prices = PricesImpl.restoreFromCache(getActivity(), tag);
            ArrayList<Coin> filtered = adapter.getItems(prices.getExchange());
            prices.copyAttrsToCoins(filtered);

            adapter.notifyDataSetChanged();
        }

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

        coins = Utils.insertSectionHeader(coins, kind.exchanges);
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

        for (Exchange exchange : kind.exchanges) {
            new GetPricesTask(ClientFactory.getInstance(getActivity()))
                    .setFromSymbols(Utils.getFromSymbols(getResources(), kind, exchange))
                    .setToSymbol(Utils.getToSymbol(getActivity(), kind))
                    .setExchange(exchange.name())
                    .setListener(this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void startAutoUpdate(boolean isRepeated) {
        if (autoUpdateTimer != null) {
            stopAutoUpdate();
        }

        String tag = getTimerTag(kind);
        if (tag == null) {
            return;
        }
        autoUpdateTimer = new AutoUpdateTimer(tag);

        if (isRepeated) {
            autoUpdateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startTask();
                }
            }, 0, PrefHelper.getSyncInterval(getActivity()));
        } else {
            startTask();
        }
    }

    private void stopAutoUpdate() {
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
    public void started(String exchange, String[] fromSymbols, String toSymbol) {
        setProgressbarVisibility(View.VISIBLE, Exchange.valueOf(exchange));
    }

    @Override
    public void finished(Prices prices) {
        if (isDetached() || getActivity() == null) {
            stopAutoUpdate();
            return;
        }

        String tag = getTimerTag(kind);
        if (autoUpdateTimer == null || tag == null || !autoUpdateTimer.getTag().equals(tag)) {
            return;
        }

        prices.saveToCache(getActivity(), kind.name() + "-" + prices.getExchange());

        ListView listView = getView().findViewById(R.id.list_view);
        ListViewAdapter adapter = (ListViewAdapter) listView.getAdapter();

        String exchange = prices.getExchange();
        ArrayList<Coin> filtered = adapter.getItems(exchange);
        prices.copyAttrsToCoins(filtered);

        adapter.notifyDataSetChanged();
        hideProgressbarDelayed(Exchange.valueOf(exchange));

        Log.d("UPDATED", exchange + ", " + new Date().toString());
    }

    private void hideProgressbarDelayed(final Exchange exchange) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setProgressbarVisibility(View.GONE, exchange);
            }
        }, ValueAnimatorBase.DURATION);
    }

    private void setProgressbarVisibility(int flag, Exchange exchange) {
        if (isDetached() || getView() == null) {
            return;
        }

        ImageView progressbar = getView().findViewWithTag(exchange.name() + "-progressbar");
        if (progressbar == null) {
            return;
        }

        if (flag == View.GONE) {
            progressbar.clearAnimation();
            progressbar.setImageResource(R.drawable.ic_refresh_stop);
            updateRelativeTimeSpanText(exchange);
        } else if (flag == View.VISIBLE) {
            progressbar.setImageResource(R.drawable.ic_refresh_rotate);
            Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
            progressbar.startAnimation(anim);
        }
    }

    private void updateRelativeTimeSpanText(Exchange exchange) {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(RelativeTimeSpanFragment.getTag(exchange, CoinImpl.Kind.trading));
        if (fragment != null) {
            ((RelativeTimeSpanFragment) fragment).setTime(System.currentTimeMillis());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isVisibleToUser) {
            startAutoUpdate(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoUpdate();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopAutoUpdate();
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

        stopAutoUpdate();
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
            startAutoUpdate(true);
        } else {
            stopAutoUpdate();
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
            stopAutoUpdate();
            startAutoUpdate(true);
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
            Fragment fragment = manager.findFragmentByTag(RelativeTimeSpanFragment.getTag(exchange, CoinImpl.Kind.trading));
            if (fragment != null) {
                transaction.remove(fragment);
            }
        }

        transaction.commitNowAllowingStateLoss();
    }

    private static class Utils {

        static String[] getFromSymbols(Resources resources, NavigationKind kind) {
            return resources.getStringArray(kind.symbolsResId);
        }

        static String[] getFromSymbols(Resources resources, NavigationKind kind, Exchange exchange) {
            String[] symbols;

            if (exchange == Exchange.cccagg) {
                symbols = getFromSymbols(resources, kind);
            } else {
                symbols = resources.getStringArray(exchange.tradingSymbolsResId);
            }

            return symbols;
        }

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

        static ArrayList<Coin> insertSectionHeader(ArrayList<Coin> coins, Exchange[] exchanges) {
            ArrayList<Coin> sectionalCoins = new ArrayList<>();

            if (exchanges.length == 1) {
                Exchange exchange = exchanges[0];
                sectionalCoins.add(exchange.createSectionHeaderCoin(CoinImpl.Kind.none));
                for (Coin coin : coins) {
                    coin.setExchange(exchange.name());
                }
                sectionalCoins.addAll(coins);
                return sectionalCoins;
            }

            for (Exchange exchange : exchanges) {
                sectionalCoins.add(exchange.createSectionHeaderCoin(CoinImpl.Kind.none));
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
                }

                sectionalCoins.addAll(sub);
            }

            return sectionalCoins;
        }
    }
}
