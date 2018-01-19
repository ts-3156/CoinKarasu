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
    private ViewPager pager;
    private TabLayout tabs;
    private View pagerContainer;
    private View infoContainer;
    private TextView warning;
    private View warningContainer;

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
                CKLog.e(TAG, e);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_exchange, container, false);

        ((TextView) view.findViewById(R.id.caption_left)).setText(getString(R.string.caption_left, coin.getSymbol(), coin.getToSymbol()));

        Spanned text = Html.fromHtml(getString(R.string.exchange_info, coin.getSymbol(), coin.getToSymbol()));
        ((TextView) view.findViewById(R.id.info_text)).setText(text);

        pager = view.findViewById(R.id.view_pager);
        tabs = view.findViewById(R.id.tab_layout);
        pagerContainer = view.findViewById(R.id.pager_container);
        infoContainer = view.findViewById(R.id.info_container);
        warning = view.findViewById(R.id.warn_text);
        warningContainer = view.findViewById(R.id.warn_container);

        startTask();

        return view;
    }

    private void createTabs(List<SnapshotCoin> coins) {
        if (tabsCreated || getActivity() == null) {
            return;
        }
        tabsCreated = true;

        CoinExchangePagerAdapter adapter = new CoinExchangePagerAdapter(getChildFragmentManager(), coins);
        pager.setAdapter(adapter);

        pager.addOnPageChangeListener(this);
        pager.setOffscreenPageLimit(Math.min(coins.size(), 5));

        tabs.setupWithViewPager(pager);

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < coins.size(); i++) {
            TabViewHolder holder = new TabViewHolder(inflater.inflate(R.layout.tab_exchange, null, false), coins.get(i));
            TabLayout.Tab tab = tabs.getTabAt(i);
            tab.setCustomView(holder.itemView);
            tab.setTag(holder);
        }

        pager.setCurrentItem(0);
        tab = tabs.getTabAt(0);
        setTabSelected(tab);

        // タブでキャッシュを利用する場合、タブのsetCustomViewよりもFragmentのonCreateViewと
        // updateTabへのコールバックが先に呼ばれてしまう。その際の不整合を防ぐために初期化完了を通知している。
        adapter.onTabsSetupFinished();
    }

    public void refreshTabText(int position, List<History> records) {
        TabLayout.Tab tab = tabs.getTabAt(position);
        if (tab == null || tab.getTag() == null) {
            if (DEBUG) CKLog.w(TAG, "refreshTabText() Cannot initialize a tab at " + position + " since it is null");
            return;
        }

        TabViewHolder holder = (TabViewHolder) tab.getTag();

        double curPrice = records.get(records.size() - 1).getClose();
        double prevPrice = records.get(0).getClose();
        holder.priceDiff = curPrice - prevPrice;
        double trend = holder.priceDiff / prevPrice;
        boolean isSelected = this.tab != null && this.tab.getPosition() == position;

        holder.trend.setText(new TrendValueFormat().format(trend));
        holder.trend.setTextColor(getResources().getColor(new TrendColorFormat().format(trend, isSelected)));
        holder.trendIcon.setImageResource(new TrendIconFormat().format(trend, isSelected));
    }

    private void setTabSelected(TabLayout.Tab tab) {
        if (tab == null || tab.getTag() == null) {
            if (DEBUG) CKLog.w(TAG, "setTabSelected() Cannot update a tab since it is null");
            return;
        }

        TabViewHolder holder = (TabViewHolder) tab.getTag();
        int activeTextColor = getResources().getColor(R.color.colorTabActiveText);

        holder.container.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        holder.label.setTextColor(activeTextColor);
        holder.price.setTextColor(activeTextColor);
        holder.trend.setTextColor(activeTextColor);
        holder.trendIcon.setImageResource(new TrendIconFormat().format(holder.priceDiff, true));
    }

    private void setTabUnselected(TabLayout.Tab tab) {
        if (tab == null || tab.getTag() == null) {
            if (DEBUG) CKLog.w(TAG, "setTabUnselected() Cannot update a tab since it is null");
            return;
        }

        TabViewHolder holder = (TabViewHolder) tab.getTag();
        int inactiveTextColor = getResources().getColor(R.color.colorTabInactiveText);

        holder.container.setBackgroundColor(Color.WHITE);
        holder.label.setTextColor(inactiveTextColor);
        holder.price.setTextColor(inactiveTextColor);
        holder.trend.setTextColor(getResources().getColor(new TrendColorFormat().format(holder.priceDiff)));
        holder.trendIcon.setImageResource(new TrendIconFormat().format(holder.priceDiff));
    }

    private void startTask() {
        if (taskStarted || errorCount >= 3 || getActivity() == null || getActivity().isFinishing()) {
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
        if (getActivity() == null || getActivity().isFinishing() || isDetached() || !isAdded()) {
            taskStarted = false;
            errorCount++;
            return;
        }

        List<SnapshotCoin> coins = snapshot.getSnapshotCoins();
        if (coins == null) {
            if (DEBUG) CKLog.w(TAG, "finished() snapshot is null " + kind + " retry=true err=" + errorCount);
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
            if (DEBUG) CKLog.w(TAG, "finished() coins is empty " + kind + " err=" + errorCount);
            pagerContainer.setVisibility(View.GONE);
            infoContainer.setVisibility(View.GONE);
            Spanned text = Html.fromHtml(getString(R.string.exchange_warn, coin.getSymbol(), coin.getToSymbol()));
            warning.setText(text);
            warningContainer.setVisibility(View.VISIBLE);
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
            int position = pager.getCurrentItem();

            if (position != tab.getPosition()) {
                setTabUnselected(tab);
                tab = tabs.getTabAt(position);
                setTabSelected(tab);
            }
        }
    }

    public boolean tabsSetupFinished() {
        return tab != null;
    }

    private static final class TabViewHolder {
        View itemView;
        View container;
        TextView label;
        TextView price;
        TextView trend;
        ImageView trendIcon;
        double priceDiff;

        TabViewHolder(View itemView, SnapshotCoin coin) {
            this.itemView = itemView;
            container = itemView.findViewById(R.id.tab_container);
            label = itemView.findViewById(R.id.label);
            price = itemView.findViewById(R.id.price);
            trend = itemView.findViewById(R.id.trend);
            trendIcon = itemView.findViewById(R.id.trend_icon);
            priceDiff = 0.0;

            label.setText(coin.getMarket());
            price.setText(new PriceFormat(coin.getToSymbol()).format(coin.getPrice()));
        }
    }
}
