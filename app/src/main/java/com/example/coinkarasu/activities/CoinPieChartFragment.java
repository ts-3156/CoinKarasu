package com.example.coinkarasu.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.coinkarasu.R;
import com.example.coinkarasu.adapters.ViewPagerAdapter;


public class CoinPieChartFragment extends Fragment implements
        ViewPager.OnPageChangeListener {

    private static final Kind DEFAULT_KIND = Kind.currency;

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

        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf");
        ((TextView) view.findViewById(R.id.caption_left)).setTypeface(typeFace);
        ((TextView) view.findViewById(R.id.caption_right)).setTypeface(typeFace);
        ((TextView) view.findViewById(R.id.caption_left)).setText(getString(R.string.caption_left, fromSymbol, toSymbol));

        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addItem(CoinPieChartTabContentFragment.newInstance(Kind.currency, fromSymbol, toSymbol));
        adapter.addItem(CoinPieChartTabContentFragment.newInstance(Kind.exchange, fromSymbol, toSymbol));

        pager = view.findViewById(R.id.view_pager);
        pager.setAdapter(adapter);
        pager.setCurrentItem(DEFAULT_KIND.ordinal());
        pager.addOnPageChangeListener(this);
        pager.setOffscreenPageLimit(Kind.values().length);

        tabs = view.findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(pager);

        tabs.getTabAt(Kind.currency.ordinal()).setCustomView(createTab(inflater, container, Kind.currency.label));
        tabs.getTabAt(Kind.exchange.ordinal()).setCustomView(createTab(inflater, container, Kind.exchange.label));

        tab = tabs.getTabAt(DEFAULT_KIND.ordinal());
        setSelected(DEFAULT_KIND.ordinal());

        return view;
    }

    private View createTab(LayoutInflater inflater, ViewGroup container, String label) {
        View view = inflater.inflate(R.layout.tab_pie_chart, container, false);

        ((TextView) view.findViewById(R.id.label)).setText(label);

        return view;
    }

    public void updateTab(int position) {
    }

    private void setSelected(int position) {
        View view = tab.getCustomView();
        view.findViewById(R.id.tab_container).setBackgroundColor(Color.WHITE);
        ((TextView) view.findViewById(R.id.label)).setTextColor(getResources().getColor(R.color.colorTabInactiveText));

        tab = tabs.getTabAt(position);
        view = tab.getCustomView();
        view.findViewById(R.id.tab_container).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        ((TextView) view.findViewById(R.id.label)).setTextColor(getResources().getColor(R.color.colorTabActiveText));
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
        pager = null;
        tabs = null;
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
            int position = pager.getCurrentItem();

            if (position != tab.getPosition()) {
                ViewPagerAdapter adapter = (ViewPagerAdapter) pager.getAdapter();
                CoinPieChartTabContentFragment fragment = (CoinPieChartTabContentFragment) adapter.getItem(position);

                setSelected(position);
                fragment.updateView();
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onPieChartKindChanged(String kind);
    }
}
