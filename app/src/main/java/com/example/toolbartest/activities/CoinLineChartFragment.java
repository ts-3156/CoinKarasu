package com.example.toolbartest.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.toolbartest.R;
import com.example.toolbartest.adapters.ViewPagerAdapter;
import com.example.toolbartest.cryptocompare.data.History;
import com.example.toolbartest.format.PriceViewFormat;
import com.example.toolbartest.format.TrendViewFormat;
import com.example.toolbartest.utils.AnimHelper;

import java.util.ArrayList;


public class CoinLineChartFragment extends Fragment implements
        ViewPager.OnPageChangeListener {

    public static final int DEFAULT_POSITION = 0;

    private OnFragmentInteractionListener listener;

    private String kind;
    private String fromSymbol;
    private String toSymbol;
    private ViewPager pager;
    private TabLayout tabs;
    private TabLayout.Tab tab;

    public CoinLineChartFragment() {
    }

    public static CoinLineChartFragment newInstance(String kind) {
        CoinLineChartFragment fragment = new CoinLineChartFragment();
        Bundle args = new Bundle();
        args.putString("kind", kind);
        fragment.setArguments(args);
        return fragment;
    }

    public static CoinLineChartFragment newInstance(String kind, String fromSymbol, String toSymbol) {
        CoinLineChartFragment fragment = new CoinLineChartFragment();
        Bundle args = new Bundle();
        args.putString("kind", kind);
        args.putString("fromSymbol", fromSymbol);
        args.putString("toSymbol", toSymbol);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            kind = getArguments().getString("kind");
            fromSymbol = getArguments().getString("fromSymbol");
            toSymbol = getArguments().getString("toSymbol");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_line_chart, container, false);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addItem(CoinLineChartTabContentFragment.newInstance("hour", fromSymbol, toSymbol, 0));
        adapter.addItem(CoinLineChartTabContentFragment.newInstance("day", fromSymbol, toSymbol, 1));
        adapter.addItem(CoinLineChartTabContentFragment.newInstance("week", fromSymbol, toSymbol, 2));
        adapter.addItem(CoinLineChartTabContentFragment.newInstance("month", fromSymbol, toSymbol, 3));
        adapter.addItem(CoinLineChartTabContentFragment.newInstance("year", fromSymbol, toSymbol, 4));

        pager = view.findViewById(R.id.view_pager);
        pager.setAdapter(adapter);
        pager.setCurrentItem(DEFAULT_POSITION);
        pager.addOnPageChangeListener(this);
        pager.setOffscreenPageLimit(5);

        tabs = view.findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(pager);

        tabs.getTabAt(0).setCustomView(createTab(inflater, container, "1 Hour"));
        tabs.getTabAt(1).setCustomView(createTab(inflater, container, "1 Day"));
        tabs.getTabAt(2).setCustomView(createTab(inflater, container, "1 Week"));
        tabs.getTabAt(3).setCustomView(createTab(inflater, container, "1 Month"));
        tabs.getTabAt(4).setCustomView(createTab(inflater, container, "1 Year"));

        tab = tabs.getTabAt(DEFAULT_POSITION);
        setSelected(DEFAULT_POSITION);

        return view;
    }

    private View createTab(LayoutInflater inflater, ViewGroup container, String label) {
        View view = inflater.inflate(R.layout.tab_custom, container, false);

        ((TextView) view.findViewById(R.id.tab_label)).setText(label);
        ((TextView) view.findViewById(R.id.tab_price)).setText("0");
        ((TextView) view.findViewById(R.id.tab_trend)).setText("0%");
        ((ImageView) view.findViewById(R.id.tab_trend_icon)).setImageResource(R.drawable.ic_trending_flat);

        return view;
    }

    public void updateTab(int position, ArrayList<History> records) {
        TabLayout.Tab tab = tabs.getTabAt(position);
        View view = tab.getCustomView();

        double curPrice = records.get(records.size() - 1).getClose();
        double prevPrice = records.get(0).getClose();

        double priceDiff = Math.round(curPrice - prevPrice);
        new PriceViewFormat(String.valueOf(priceDiff), records.get(0).getToSymbol())
                .format((TextView) view.findViewById(R.id.tab_price));

        boolean isSelected = this.tab != null && this.tab.getPosition() == position;
        double trend = priceDiff / prevPrice;
        new TrendViewFormat(String.valueOf(trend))
                .format((TextView) view.findViewById(R.id.tab_trend), !isSelected);

        if (isSelected) {
            AnimHelper.setWhiteTrendIcon((ImageView) view.findViewById(R.id.tab_trend_icon), trend);
        } else {
            AnimHelper.setTrendIcon((ImageView) view.findViewById(R.id.tab_trend_icon), trend);
        }
    }

    private void setSelected(int position) {
        View view = tab.getCustomView();
        view.findViewById(R.id.tab_container).setBackgroundColor(Color.WHITE);
        ((TextView) view.findViewById(R.id.tab_label)).setTextColor(Color.parseColor("#80000000"));
        ((TextView) view.findViewById(R.id.tab_price)).setTextColor(Color.parseColor("#80000000"));

        TextView trend = view.findViewById(R.id.tab_trend);
        trend.setTextColor(Color.parseColor("#80000000"));
        AnimHelper.setTrendIcon((ImageView) view.findViewById(R.id.tab_trend_icon), parseTrendValue(trend.getText()));

        tab = tabs.getTabAt(position);
        view = tab.getCustomView();
        view.findViewById(R.id.tab_container).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        ((TextView) view.findViewById(R.id.tab_label)).setTextColor(Color.WHITE);
        ((TextView) view.findViewById(R.id.tab_price)).setTextColor(Color.WHITE);

        trend = view.findViewById(R.id.tab_trend);
        trend.setTextColor(Color.WHITE);
        AnimHelper.setWhiteTrendIcon((ImageView) view.findViewById(R.id.tab_trend_icon), parseTrendValue(trend.getText()));
    }

    private double parseTrendValue(CharSequence str) {
        String str2 = str.toString().replace("%", "").replace(",", "");
        return Double.valueOf(str2);
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
                ViewPagerAdapter adapter = (ViewPagerAdapter) pager.getAdapter();
                CoinLineChartTabContentFragment fragment = (CoinLineChartTabContentFragment) adapter.getItem(position);

                setSelected(position);
                fragment.updateView(true);
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onLineChartKindChanged(String kind);
    }
}
