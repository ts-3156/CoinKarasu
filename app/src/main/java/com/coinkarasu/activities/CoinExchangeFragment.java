package com.coinkarasu.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinkarasu.R;
import com.coinkarasu.api.cryptocompare.ClientFactory;
import com.coinkarasu.api.cryptocompare.data.CoinSnapshot;
import com.coinkarasu.api.cryptocompare.data.History;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.coins.CoinImpl;
import com.coinkarasu.coins.SnapshotCoin;
import com.coinkarasu.format.PriceFormat;
import com.coinkarasu.format.TrendColorFormat;
import com.coinkarasu.format.TrendIconFormat;
import com.coinkarasu.format.TrendValueFormat;
import com.coinkarasu.pagers.CoinExchangePagerAdapter;
import com.coinkarasu.tasks.GetCoinSnapshotTask;
import com.coinkarasu.utils.CKLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


public class CoinExchangeFragment extends Fragment implements
        GetCoinSnapshotTask.Listener, ViewPager.OnPageChangeListener {

    private static final boolean DEBUG = true;
    private static final String TAG = "CoinExchangeFragment";

    private String kind;
    private Coin coin;
    private boolean taskStarted;
    private int errorCount = 0;

    private boolean tabsCreated = false;
    private TabLayout.Tab tab;

    public CoinExchangeFragment() {
    }

    public static CoinExchangeFragment newInstance(String kind, String coinJson) {
        CoinExchangeFragment fragment = new CoinExchangeFragment();
        Bundle args = new Bundle();
        args.putString("kind", kind);
        args.putString("coinJson", coinJson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            kind = getArguments().getString("kind");
            String coinJson = getArguments().getString("coinJson");

            try {
                coin = CoinImpl.buildByAttrs(new JSONObject(coinJson));
            } catch (JSONException e) {
                if (DEBUG) CKLog.e(TAG, e);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_exchange, container, false);

        ((TextView) view.findViewById(R.id.caption_left)).setText(getString(R.string.caption_left, coin.getSymbol(), coin.getToSymbol()));

        Spanned text = Html.fromHtml(getString(R.string.exchange_info, coin.getSymbol(), coin.getToSymbol()));
        ((TextView) view.findViewById(R.id.info_text)).setText(text);

        startTask();

        return view;
    }

    private void createTabs(List<SnapshotCoin> coins) {
        if (tabsCreated || getView() == null || getActivity() == null) {
            return;
        }
        tabsCreated = true;

        View view = getView();
        ViewPager pager = view.findViewById(R.id.view_pager);
        pager.setAdapter(new CoinExchangePagerAdapter(getChildFragmentManager(), coins));

        int offset = pager.getAdapter().getCount() - coins.size();
        pager.setCurrentItem(offset);
        pager.addOnPageChangeListener(this);
        pager.setOffscreenPageLimit(Math.min(coins.size() + offset, 5));

        TabLayout tabs = view.findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(pager);

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < offset; i++) {
            tabs.getTabAt(i).setCustomView(createTab(inflater, null, "All"));
        }
        for (int i = 0; i < coins.size(); i++) {
            SnapshotCoin coin = coins.get(i);
            tabs.getTabAt(i + offset).setCustomView(createTab(inflater, null, coin));
        }

        tab = tabs.getTabAt(offset);
        setSelected(offset, view);
    }

    private View createTab(LayoutInflater inflater, ViewGroup container, String label) {
        View view = inflater.inflate(R.layout.tab_exchanges, container, false);
        ((TextView) view.findViewById(R.id.label)).setText(label);
        return view;
    }

    private View createTab(LayoutInflater inflater, ViewGroup container, SnapshotCoin coin) {
        View view = inflater.inflate(R.layout.tab_exchange, container, false);

        ((TextView) view.findViewById(R.id.label)).setText(coin.getMarket());

        String priceString = new PriceFormat(coin.getToSymbol()).format(coin.getPrice());
        ((TextView) view.findViewById(R.id.price)).setText(priceString);

        ((TextView) view.findViewById(R.id.trend)).setText("0.00%");
        ((ImageView) view.findViewById(R.id.trend_icon)).setImageResource(R.drawable.ic_trending_flat);

        return view;
    }

    public void updateTab(int position, List<History> records) {
        View container = getView();
        if (container == null) {
            return;
        }

        TabLayout tabs = container.findViewById(R.id.tab_layout);
        if (tabs == null) {
            return;
        }

        TabLayout.Tab tab = tabs.getTabAt(position);
        View view = tab.getCustomView();

        double curPrice = records.get(records.size() - 1).getClose();
        double prevPrice = records.get(0).getClose();
        double priceDiff = curPrice - prevPrice;

        boolean isSelected = this.tab != null && this.tab.getPosition() == position;

        double trend = priceDiff / prevPrice;
        TextView trendView = view.findViewById(R.id.trend);
        trendView.setText(new TrendValueFormat().format(trend));
        trendView.setTextColor(getResources().getColor(new TrendColorFormat().format(trend, isSelected)));

        ImageView icon = view.findViewById(R.id.trend_icon);
        icon.setImageResource(new TrendIconFormat().format(trend, isSelected));

        tab.setTag(priceDiff);
    }

    private void setSelected(int position, View container) {
        if (tab == null || container == null) {
            return;
        }

        int inactiveTextColor = getResources().getColor(R.color.colorTabInactiveText);
        View view = tab.getCustomView();
        view.findViewById(R.id.tab_container).setBackgroundColor(Color.WHITE);
        ((TextView) view.findViewById(R.id.label)).setTextColor(inactiveTextColor);
        ((TextView) view.findViewById(R.id.price)).setTextColor(inactiveTextColor);

        ((TextView) view.findViewById(R.id.trend)).setTextColor(inactiveTextColor);
        Object tag = tab.getTag();
        double priceDiff = tag == null ? 0 : (double) tag;
        ((TextView) view.findViewById(R.id.trend))
                .setTextColor(getResources().getColor(new TrendColorFormat().format(priceDiff)));
        ((ImageView) view.findViewById(R.id.trend_icon))
                .setImageResource(new TrendIconFormat().format(priceDiff));

        tab = ((TabLayout) container.findViewById(R.id.tab_layout)).getTabAt(position);

        int activeTextColor = getResources().getColor(R.color.colorTabActiveText);
        view = tab.getCustomView();
        view.findViewById(R.id.tab_container).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        ((TextView) view.findViewById(R.id.label)).setTextColor(activeTextColor);
        ((TextView) view.findViewById(R.id.price)).setTextColor(activeTextColor);

        tag = tab.getTag();
        priceDiff = tag == null ? 0 : (double) tag;
        ((TextView) view.findViewById(R.id.trend)).setTextColor(activeTextColor);
        ((ImageView) view.findViewById(R.id.trend_icon))
                .setImageResource(new TrendIconFormat().format(priceDiff, true));
    }

    private void startTask() {
        if (taskStarted || errorCount >= 3 || getActivity() == null) {
            return;
        }
        taskStarted = true;

        new GetCoinSnapshotTask(ClientFactory.getInstance(getActivity()))
                .setFromSymbol(coin.getSymbol())
                .setToSymbol(coin.getToSymbol())
                .setListener(this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void finished(CoinSnapshot snapshot) {
        if (isDetached() || getView() == null) {
            taskStarted = false;
            errorCount++;
            return;
        }

        List<SnapshotCoin> coins = snapshot.getSnapshotCoins();
        if (coins == null) {
            Log.e("finished", "null(retry), " + kind + ", " + errorCount);
            taskStarted = false;
            errorCount++;
            startTask();
            return;
        }

        Iterator<SnapshotCoin> iterator = coins.iterator();
        while (iterator.hasNext()) {
            SnapshotCoin coin = iterator.next();
            if (coin.getVolume24Hour() <= 0.0 || coin.getMarket().equals("LocalBitcoins")) {
                iterator.remove();
            }
        }

        if (coins.isEmpty()) {
            Log.e("finished", "empty, " + kind + ", " + errorCount);
            View view = getView();
            view.findViewById(R.id.pager_container).setVisibility(View.GONE);
            view.findViewById(R.id.info_container).setVisibility(View.GONE);
            Spanned text = Html.fromHtml(getString(R.string.exchange_warn, coin.getSymbol(), coin.getToSymbol()));
            ((TextView) view.findViewById(R.id.warn_text)).setText(text);
            view.findViewById(R.id.warn_container).setVisibility(View.VISIBLE);
            return;
        }

        Collections.sort(coins, new Comparator<SnapshotCoin>() {
            public int compare(SnapshotCoin c1, SnapshotCoin c2) {
                return c1.getPrice() > c2.getPrice() ? -1 : 1;
            }
        });

        createTabs(coins);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        kind = null;
        coin = null;
        tab = null;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_SETTLING) {
            if (getView() == null) {
                return;
            }

            int position = ((ViewPager) getView().findViewById(R.id.view_pager)).getCurrentItem();

            if (position != tab.getPosition()) {
                setSelected(position, getView());
            }
        }
    }
}
