package com.example.coinkarasu.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.coinkarasu.R;
import com.example.coinkarasu.adapters.ListViewAdapter;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.coins.SectionHeaderCoinImpl;
import com.example.coinkarasu.cryptocompare.Client;
import com.example.coinkarasu.cryptocompare.data.Prices;
import com.example.coinkarasu.format.ValueAnimatorBase;
import com.example.coinkarasu.tasks.GetPricesTask;
import com.example.coinkarasu.utils.AutoUpdateTimer;
import com.example.coinkarasu.utils.ExchangeImpl;
import com.example.coinkarasu.utils.PrefHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;


public class ListViewFragment extends Fragment
        implements AdapterView.OnItemClickListener, ListView.OnScrollListener, GetPricesTask.Listener {

    private enum NavigationKind {
        nav_main(R.array.japan_all_symbols, new String[]{"bitflyer", "coincheck", "zaif"}),
        jpy_toplist(R.array.jpy_toplist_symbols, new String[]{"cccagg"}),
        usd_toplist(R.array.usd_toplist_symbols, new String[]{"cccagg"}),
        eur_toplist(R.array.eur_toplist_symbols, new String[]{"cccagg"}),
        btc_toplist(R.array.btc_toplist_symbols, new String[]{"cccagg"});

        int symbolsResId;
        String[] exchanges;

        NavigationKind(int symbolsResId, String[] exchanges) {
            this.symbolsResId = symbolsResId;
            this.exchanges = exchanges;
        }
    }

    private enum Exchange {
        bitflyer(R.array.bitflyer_symbols),
        coincheck(R.array.coincheck_symbols),
        zaif(R.array.zaif_symbols);

        int symbolsResId;

        Exchange(int symbolsResId) {
            this.symbolsResId = symbolsResId;
        }

        public static boolean contains(String value) {
            for (Exchange ex : values()) {
                if (ex.name().equals(value)) {
                    return true;
                }
            }

            return false;
        }
    }

    private OnFragmentInteractionListener listener;

    private AutoUpdateTimer autoUpdateTimer;

    private Client client;
    private NavigationKind kind;
    private boolean isVisibleToUser = false;

    public ListViewFragment() {
    }

    public static ListViewFragment newInstance(MainActivity.NavigationKind kind) {
        ListViewFragment fragment = new ListViewFragment();
        Bundle args = new Bundle();
        args.putString("kind", kind.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            kind = NavigationKind.valueOf(getArguments().getString("kind"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_view, container, false);
        Activity activity = getActivity();

        client = ((MainActivity) activity).getClient();

        ArrayList<Coin> coins = ((MainActivity) activity).collectCoins(getFromSymbols(kind), getToSymbol(kind));
        coins = insertSectionHeader(coins, kind.exchanges);

        ListViewAdapter adapter = new ListViewAdapter(activity, coins);
        ListView listView = view.findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);
        ViewCompat.setNestedScrollingEnabled(listView, true); // <= 21 http://starzero.hatenablog.com/entry/2015/09/30/114136

        startAutoUpdate(0, MainActivity.DEFAULT_KIND == MainActivity.NavigationKind.valueOf(kind.name()));

        return view;
    }

    public void updateView() {
        if (isDetached() || getView() == null) {
            return;
        }

        if (autoUpdateTimer != null) {
            return;
        }

        startAutoUpdate(0, true);
    }

    private void startTask() {
        if (!isAdded() || getActivity() == null || isDetached() || getView() == null) {
            return;
        }

        ListView listView = getView().findViewById(R.id.list_view);
        ListViewAdapter adapter = (ListViewAdapter) listView.getAdapter();

        adapter.setAnimEnabled(PrefHelper.isAnimEnabled(getActivity()));
        adapter.setDownloadIconEnabled(PrefHelper.isDownloadIconEnabled(getActivity()));
        adapter.setToSymbol(getToSymbol(kind));

        for (String exchange : kind.exchanges) {
            new GetPricesTask(client)
                    .setFromSymbols(getFromSymbolsByExchange(exchange))
                    .setToSymbol(getToSymbol(kind))
                    .setExchange(exchange)
                    .setListener(this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void startAutoUpdate(int delay, boolean isRepeated) {
        if (autoUpdateTimer != null) {
            stopAutoUpdate();
        }

        autoUpdateTimer = new AutoUpdateTimer(getTimerTag(kind));

        if (isRepeated) {
            autoUpdateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startTask();
                }
            }, delay, PrefHelper.getSyncInterval(getActivity()));
        } else {
            startTask();
        }
    }

    public void stopAutoUpdate() {
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
            autoUpdateTimer = null;
        }
    }

    private String getTimerTag(NavigationKind kind) {
        String suffix = getToSymbol(kind);
        if (suffix == null) {
            return null;
        }
        return kind.name() + "-" + getToSymbol(kind);
    }

    @Override
    public void started(String exchange, String[] fromSymbols, String toSymbol) {
        setProgressbarVisibility(true, exchange);
    }

    @Override
    public void finished(Prices prices) {
        String tag = getTimerTag(kind);
        if (autoUpdateTimer == null || tag == null || !autoUpdateTimer.getTag().equals(tag)) {
            return;
        }

        if (isDetached() || getView() == null) {
            return;
        }

        ListView listView = getView().findViewById(R.id.list_view);
        ListViewAdapter adapter = (ListViewAdapter) listView.getAdapter();

        String exchange = prices.getExchange();
        ArrayList<Coin> filtered = adapter.getItems(exchange);
        prices.setAttrsToCoins(filtered);

        adapter.notifyDataSetChanged();
        hideProgressbarDelayed(exchange);

        Log.d("UPDATED", exchange + ", " + new Date().toString());
    }

    private void hideProgressbarDelayed(final String exchange) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setProgressbarVisibility(false, exchange);
            }
        }, ValueAnimatorBase.DURATION);
    }

    public void setProgressbarVisibility(boolean flag, String exchange) {
        if (isDetached() || getView() == null) {
            return;
        }

        View progressbar = getView().findViewWithTag(exchange);
        if (progressbar == null) {
            return;
        }

        if (flag) {
            if (progressbar.getVisibility() != View.VISIBLE) {
                progressbar.setVisibility(View.VISIBLE);
            }
        } else {
            if (progressbar.getVisibility() != View.GONE) {
                progressbar.setVisibility(View.GONE);
            }
        }
    }

    private ArrayList<Coin> insertSectionHeader(ArrayList<Coin> coins, String[] exchanges) {
        ArrayList<Coin> sectionalCoins = new ArrayList<>();

        if (exchanges.length == 1) {
            sectionalCoins.add(new SectionHeaderCoinImpl(ExchangeImpl.exchangeToDisplayName(exchanges[0]), exchanges[0]));
            for (Coin coin : coins) {
                coin.setExchange(exchanges[0]);
            }
            sectionalCoins.addAll(coins);
            return sectionalCoins;
        }

        for (String exchange : exchanges) {
            sectionalCoins.add(new SectionHeaderCoinImpl(ExchangeImpl.exchangeToDisplayName(exchange), exchange));
            List<Coin> sub;

            switch (exchange) {
                case "bitflyer":
                    sub = coins.subList(0, 1);
                    break;
                case "coincheck":
                    sub = coins.subList(1, 2);
                    break;
                case "zaif":
                    sub = coins.subList(2, coins.size());
                    break;
                default:
                    throw new RuntimeException("Invalid exchange " + exchange);
            }

            for (Coin coin : sub) {
                coin.setExchange(exchange);
            }

            sectionalCoins.addAll(sub);
        }

        return sectionalCoins;
    }

    private String[] getFromSymbols(NavigationKind kind) {
        return getResources().getStringArray(kind.symbolsResId);
    }

    private String[] getFromSymbolsByExchange(String exchange) {
        String[] symbols;

        if (Exchange.contains(exchange)) {
            symbols = getResources().getStringArray(Exchange.valueOf(exchange).symbolsResId);
        } else {
            symbols = getFromSymbols(kind);
        }

        return symbols;
    }

    private String getToSymbol(NavigationKind kind) {
        String symbol;

        if (kind == NavigationKind.nav_main) {
            symbol = MainActivity.Currency.JPY.name();
        } else {
            if (getActivity() == null) {
                return null;
            }
            symbol = PrefHelper.getToSymbol(getActivity());
        }

        return symbol;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (autoUpdateTimer == null) {
            startAutoUpdate(0, isVisibleToUser);
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
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnFragmentInteractionListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
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
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
