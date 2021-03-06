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
import android.widget.TextView;

import com.coinkarasu.R;
import com.coinkarasu.activities.etc.PieChartKind;
import com.coinkarasu.api.cryptocompare.ClientFactory;
import com.coinkarasu.api.cryptocompare.data.TopPairs;
import com.coinkarasu.chart.CoinPieChart;
import com.coinkarasu.coins.TopPairCoin;
import com.coinkarasu.pagers.CoinPieChartPagerAdapter;
import com.coinkarasu.tasks.GetTopPairsTask;
import com.coinkarasu.utils.CKLog;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


public class CoinPieChartFragment extends Fragment implements
        ViewPager.OnPageChangeListener, GetTopPairsTask.Listener {

    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "CoinPieChartFragment";
    private static final PieChartKind DEFAULT_KIND = PieChartKind.currency;

    private boolean taskStarted;
    private int errorCount = 0;
    private boolean tabsCreated = false;
    private TabLayout.Tab tab;
    private String fromSymbol;
    private String toSymbol;
    private ViewPager pager;
    private TabLayout tabs;

    public CoinPieChartFragment() {
    }

    public static CoinPieChartFragment newInstance(String fromSymbol, String toSymbol) {
        CoinPieChartFragment fragment = new CoinPieChartFragment();
        Bundle args = new Bundle();
        args.putString("fromSymbol", fromSymbol);
        args.putString("toSymbol", toSymbol);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fromSymbol = getArguments().getString("fromSymbol");
            toSymbol = getArguments().getString("toSymbol");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_pie_chart, container, false);

        ((TextView) view.findViewById(R.id.caption_left)).setText(fromSymbol);

        Spanned text = Html.fromHtml(getString(R.string.pie_chart_info, fromSymbol));
        ((TextView) view.findViewById(R.id.info_text)).setText(text);

        pager = view.findViewById(R.id.view_pager);
        tabs = view.findViewById(R.id.tab_layout);

        startTask();

        return view;
    }

    private void createTabs(List<TopPairCoin> coins) {
        if (tabsCreated || getView() == null || getActivity() == null) {
            return;
        }
        tabsCreated = true;

        pager.setAdapter(new CoinPieChartPagerAdapter(getChildFragmentManager(), fromSymbol, toSymbol, coins));
        pager.setCurrentItem(DEFAULT_KIND.ordinal());
        pager.addOnPageChangeListener(this);
        pager.setOffscreenPageLimit(Math.min(coins.size() + 1, 5));

        tabs.setupWithViewPager(pager);

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        TabViewHolder firstHolder = new TabViewHolder(inflater.inflate(R.layout.tab_pie_chart, null, false), PieChartKind.currency.label, fromSymbol);
        TabLayout.Tab firstTab = tabs.getTabAt(PieChartKind.currency.ordinal());
        firstTab.setCustomView(firstHolder.itemView);
        firstTab.setTag(firstHolder);

        for (int i = 0; i < coins.size(); i++) {
            TabViewHolder holder = new TabViewHolder(inflater.inflate(R.layout.tab_pie_chart, null, false), PieChartKind.exchange.label, coins.get(i).getToSymbol());
            TabLayout.Tab tab = tabs.getTabAt(i + 1);
            tab.setCustomView(holder.itemView);
            tab.setTag(holder);
        }

        tab = tabs.getTabAt(DEFAULT_KIND.ordinal());
        setTabSelected(tab);
    }

    private void setTabSelected(TabLayout.Tab tab) {
        if (tab == null || tab.getTag() == null) {
            if (DEBUG) CKLog.w(TAG, "setTabSelected() Cannot update a tab since it is null");
            return;
        }

        TabViewHolder holder = (TabViewHolder) tab.getTag();
        int activeColor = getResources().getColor(R.color.colorTabActiveText);

        holder.container.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        holder.label.setTextColor(activeColor);
        holder.symbol.setTextColor(activeColor);
    }

    private void setTabUnselected(TabLayout.Tab tab) {
        if (tab == null || tab.getTag() == null) {
            if (DEBUG) CKLog.w(TAG, "setTabUnselected() Cannot update a tab since it is null");
            return;
        }

        TabViewHolder holder = (TabViewHolder) tab.getTag();
        int inactiveColor = getResources().getColor(R.color.colorTabInactiveText);

        holder.container.setBackgroundColor(Color.WHITE);
        holder.label.setTextColor(inactiveColor);
        holder.symbol.setTextColor(inactiveColor);
    }

    private void startTask() {
        if (taskStarted || errorCount >= 3 || getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        taskStarted = true;

        new GetTopPairsTask(ClientFactory.getInstance(getActivity()))
                .setFromSymbol(fromSymbol)
                .setListener(this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void finished(TopPairs topPairs) {
        if (getActivity() == null || getActivity().isFinishing() || isDetached() || !isAdded()) {
            taskStarted = false;
            errorCount++;
            return;
        }

        List<TopPairCoin> coins = topPairs.getTopPairCoins();
        if (coins == null) {
            if (DEBUG) CKLog.w(TAG, "finished() pairs is null " + "retry=true err=" + errorCount);
            taskStarted = false;
            errorCount++;
            startTask();
            return;
        }

        double sum = 0.0;
        for (TopPairCoin coin : coins) {
            sum += coin.getVolume24h();
        }
        double threshold = sum * CoinPieChart.GROUP_SMALL_SLICES_PCT;

        Iterator<TopPairCoin> iterator = coins.iterator();
        while (iterator.hasNext()) {
            TopPairCoin coin = iterator.next();
            if (coin.getVolume24h() < threshold) {
                iterator.remove();
            }
        }

        Collections.sort(coins, new Comparator<TopPairCoin>() {
            public int compare(TopPairCoin c1, TopPairCoin c2) {
                return c1.getVolume24h() > c2.getVolume24h() ? -1 : 1;
            }
        });

        createTabs(coins);
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

    private static final class TabViewHolder {
        View itemView;
        View container;
        TextView label;
        TextView symbol;

        TabViewHolder(View itemView, String labelString, String symbolString) {
            this.itemView = itemView;
            container = itemView.findViewById(R.id.tab_container);
            label = itemView.findViewById(R.id.label);
            symbol = itemView.findViewById(R.id.symbol);

            label.setText(labelString);
            symbol.setText(symbolString);
        }
    }
}
