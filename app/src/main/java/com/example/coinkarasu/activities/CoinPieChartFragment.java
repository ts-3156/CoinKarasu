package com.example.coinkarasu.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.coinkarasu.adapters.ViewPagerAdapter;
import com.example.coinkarasu.R;


public class CoinPieChartFragment extends Fragment implements
        ViewPager.OnPageChangeListener {

    public static final int DEFAULT_POSITION = 0;

    public enum Kind {
        currency("Money flow"),
        exchange("Trading volume");

        String label;

        Kind(String label) {
            this.label = label;
        }
    }

    private OnFragmentInteractionListener listener;

    private ViewPager pager;
    private TabLayout tabs;
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

        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addItem(CoinPieChartTabContentFragment.newInstance(Kind.currency.name(), fromSymbol, toSymbol, 0));
        adapter.addItem(CoinPieChartTabContentFragment.newInstance(Kind.exchange.name(), fromSymbol, toSymbol, 1));

        pager = view.findViewById(R.id.view_pager);
        pager.setAdapter(adapter);
        pager.setCurrentItem(DEFAULT_POSITION);
        pager.addOnPageChangeListener(this);
        pager.setOffscreenPageLimit(2);

        tabs = view.findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(pager);

        tabs.getTabAt(0).setCustomView(createTab(inflater, container, Kind.currency.label));
        tabs.getTabAt(1).setCustomView(createTab(inflater, container, Kind.exchange.label));

        tab = tabs.getTabAt(DEFAULT_POSITION);
        setSelected(DEFAULT_POSITION);

        return view;
    }

    private View createTab(LayoutInflater inflater, ViewGroup container, String label) {
        View view = inflater.inflate(R.layout.tab_pie_chart, container, false);

        ((TextView) view.findViewById(R.id.tab_label)).setText(label);

        return view;
    }

    public void updateTab(int position) {
    }

    private void setSelected(int position) {
        View view = tab.getCustomView();
        view.findViewById(R.id.tab_container).setBackgroundColor(Color.WHITE);
        ((TextView) view.findViewById(R.id.tab_label)).setTextColor(Color.parseColor("#80000000"));

        tab = tabs.getTabAt(position);
        view = tab.getCustomView();
        view.findViewById(R.id.tab_container).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        ((TextView) view.findViewById(R.id.tab_label)).setTextColor(Color.WHITE);
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
                CoinPieChartTabContentFragment fragment = (CoinPieChartTabContentFragment) adapter.getItem(position);

                setSelected(position);
                fragment.updateView(true);
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onPieChartKindChanged(String kind);
    }
}
