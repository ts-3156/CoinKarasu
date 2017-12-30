package com.example.coinkarasu.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.TextView;

import com.example.coinkarasu.R;
import com.example.coinkarasu.api.cryptocompare.ClientFactory;
import com.example.coinkarasu.api.cryptocompare.data.TopPair;
import com.example.coinkarasu.pagers.CoinPieChartPagerAdapter;
import com.example.coinkarasu.tasks.GetTopPairsTask;
import com.example.coinkarasu.utils.AssetsHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;


public class CoinPieChartFragment extends Fragment implements
        ViewPager.OnPageChangeListener, GetTopPairsTask.Listener {

    private static final Kind DEFAULT_KIND = Kind.currency;

    public enum Kind {
        currency("Money flow"),
        exchange("Trading volume");

        String label;

        Kind(String label) {
            this.label = label;
        }
    }

    private boolean taskStarted;
    private int errorCount = 0;
    private boolean tabsCreated = false;
    private TabLayout.Tab tab;
    private String fromSymbol;
    private String toSymbol;

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

        Typeface typeFace = AssetsHelper.getInstance(getActivity()).light;
        ((TextView) view.findViewById(R.id.caption_left)).setTypeface(typeFace);
        ((TextView) view.findViewById(R.id.caption_right)).setTypeface(typeFace);
        ((TextView) view.findViewById(R.id.caption_desc_left)).setTypeface(typeFace);

        typeFace = AssetsHelper.getInstance(getActivity()).lightItalic;
        ((TextView) view.findViewById(R.id.caption_desc_right)).setTypeface(typeFace);

        ((TextView) view.findViewById(R.id.caption_left)).setText(fromSymbol);

        Spanned text = Html.fromHtml(getString(R.string.line_chart_info, fromSymbol, toSymbol));
        ((TextView) view.findViewById(R.id.info_text)).setText(text); // TODO Change text

        startTask();

        return view;
    }

    private void createTabs(ArrayList<TopPair> pairs) {
        if (tabsCreated || getView() == null || getActivity() == null) {
            return;
        }
        tabsCreated = true;

        View view = getView();
        ViewPager pager = view.findViewById(R.id.view_pager);
        pager.setAdapter(new CoinPieChartPagerAdapter(getChildFragmentManager(), fromSymbol, toSymbol, pairs));
        pager.setCurrentItem(DEFAULT_KIND.ordinal());
        pager.addOnPageChangeListener(this);
        pager.setOffscreenPageLimit(Math.min(pairs.size() + 1, 5));

        TabLayout tabs = view.findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(pager);

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        tabs.getTabAt(Kind.currency.ordinal()).setCustomView(createTab(inflater, null, Kind.currency.label, fromSymbol));
        for (int i = 0; i < pairs.size(); i++) {
            tabs.getTabAt(i + 1).setCustomView(createTab(inflater, null, Kind.exchange.label, pairs.get(i).getToSymbol()));
        }

        tab = tabs.getTabAt(DEFAULT_KIND.ordinal());
        setSelected(DEFAULT_KIND.ordinal(), view);
    }

    private View createTab(LayoutInflater inflater, ViewGroup container, String label, String symbol) {
        View view = inflater.inflate(R.layout.tab_pie_chart, container, false);

        ((TextView) view.findViewById(R.id.label)).setText(label);
        ((TextView) view.findViewById(R.id.symbol)).setText(symbol);

        return view;
    }

    private void setSelected(int position, View container) {
        if (container == null) {
            return;
        }

        int inactiveColor = getResources().getColor(R.color.colorTabInactiveText);
        View view = tab.getCustomView();
        view.findViewById(R.id.tab_container).setBackgroundColor(Color.WHITE);
        ((TextView) view.findViewById(R.id.label)).setTextColor(inactiveColor);
        ((TextView) view.findViewById(R.id.symbol)).setTextColor(inactiveColor);

        tab = ((TabLayout) container.findViewById(R.id.tab_layout)).getTabAt(position);

        int activeColor = getResources().getColor(R.color.colorTabActiveText);
        view = tab.getCustomView();
        view.findViewById(R.id.tab_container).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        ((TextView) view.findViewById(R.id.label)).setTextColor(activeColor);
        ((TextView) view.findViewById(R.id.symbol)).setTextColor(activeColor);
    }

    private void startTask() {
        if (taskStarted || errorCount >= 3 || getActivity() == null) {
            return;
        }
        taskStarted = true;

        new GetTopPairsTask(ClientFactory.getInstance(getActivity()))
                .setFromSymbol(fromSymbol)
                .setListener(this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void finished(ArrayList<TopPair> pairs) {
        if (isDetached() || getView() == null) {
            taskStarted = false;
            errorCount++;
            return;
        }

        if (pairs == null) {
            Log.e("finished", "null(retry), " + errorCount);
            taskStarted = false;
            errorCount++;
            startTask();
            return;
        }

        double sum = 0.0;
        for (TopPair pair : pairs) {
            sum += pair.getVolume24h();
        }
        double threshold = sum * CoinPieChartTabContentFragment.GROUP_SMALL_SLICES_PCT;

        Iterator<TopPair> iterator = pairs.iterator();
        while (iterator.hasNext()) {
            TopPair pair = iterator.next();
            if (pair.getVolume24h() < threshold) {
                iterator.remove();
            }
        }

        Collections.sort(pairs, new Comparator<TopPair>() {
            public int compare(TopPair tp1, TopPair tp2) {
                return tp1.getVolume24h() > tp2.getVolume24h() ? -1 : 1;
            }
        });

        createTabs(pairs);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        tab = null;
        fromSymbol = null;
        toSymbol = null;
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
