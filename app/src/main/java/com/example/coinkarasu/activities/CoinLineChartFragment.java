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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.coinkarasu.R;
import com.example.coinkarasu.adapters.ViewPagerAdapter;
import com.example.coinkarasu.cryptocompare.data.History;
import com.example.coinkarasu.format.PriceFormat;
import com.example.coinkarasu.format.TrendColorFormat;
import com.example.coinkarasu.format.TrendValueFormat;
import com.example.coinkarasu.utils.IconHelper;

import java.util.ArrayList;


public class CoinLineChartFragment extends Fragment implements
        ViewPager.OnPageChangeListener {

    public static final int DEFAULT_POSITION = 0;
    private static final Kind DEFAULT_KIND = Kind.hour;

    public enum Kind {
        hour("1 Hour"),
        day("1 Day"),
        week("1 Week"),
        month("1 Month"),
        year("1 Year");

        String label;

        Kind(String label) {
            this.label = label;
        }
    }

    private OnFragmentInteractionListener listener;

    private String fromSymbol;
    private String toSymbol;
    private ViewPager pager;
    private TabLayout tabs;
    private TabLayout.Tab tab;

    public CoinLineChartFragment() {
    }

    public static CoinLineChartFragment newInstance(String fromSymbol, String toSymbol) {
        CoinLineChartFragment fragment = new CoinLineChartFragment();
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
        View view = inflater.inflate(R.layout.fragment_coin_line_chart, container, false);

        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf");
        ((TextView) view.findViewById(R.id.caption_left)).setTypeface(typeFace);
        ((TextView) view.findViewById(R.id.caption_right)).setTypeface(typeFace);
        ((TextView) view.findViewById(R.id.caption_desc_left)).setTypeface(typeFace);

        typeFace = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-LightItalic.ttf");
        ((TextView) view.findViewById(R.id.caption_desc_right)).setTypeface(typeFace);

        ((TextView) view.findViewById(R.id.caption_left)).setText(getString(R.string.caption_left, fromSymbol, toSymbol));

        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addItem(CoinLineChartTabContentFragment.newInstance(Kind.hour, fromSymbol, toSymbol));
        adapter.addItem(CoinLineChartTabContentFragment.newInstance(Kind.day, fromSymbol, toSymbol));
        adapter.addItem(CoinLineChartTabContentFragment.newInstance(Kind.week, fromSymbol, toSymbol));
        adapter.addItem(CoinLineChartTabContentFragment.newInstance(Kind.month, fromSymbol, toSymbol));
        adapter.addItem(CoinLineChartTabContentFragment.newInstance(Kind.year, fromSymbol, toSymbol));

        pager = view.findViewById(R.id.view_pager);
        pager.setAdapter(adapter);
        pager.setCurrentItem(DEFAULT_KIND.ordinal());
        pager.addOnPageChangeListener(this);
        pager.setOffscreenPageLimit(Kind.values().length);

        tabs = view.findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(pager);

        tabs.getTabAt(Kind.hour.ordinal()).setCustomView(createTab(inflater, container, Kind.hour.label));
        tabs.getTabAt(Kind.day.ordinal()).setCustomView(createTab(inflater, container, Kind.day.label));
        tabs.getTabAt(Kind.week.ordinal()).setCustomView(createTab(inflater, container, Kind.week.label));
        tabs.getTabAt(Kind.month.ordinal()).setCustomView(createTab(inflater, container, Kind.month.label));
        tabs.getTabAt(Kind.year.ordinal()).setCustomView(createTab(inflater, container, Kind.year.label));

        tab = tabs.getTabAt(DEFAULT_KIND.ordinal());
        setSelected(DEFAULT_KIND.ordinal());

        return view;
    }

    private View createTab(LayoutInflater inflater, ViewGroup container, String label) {
        View view = inflater.inflate(R.layout.tab_line_chart, container, false);

        ((TextView) view.findViewById(R.id.tab_label)).setText(label);
        ((TextView) view.findViewById(R.id.tab_price)).setText("0.00");
        ((TextView) view.findViewById(R.id.tab_trend)).setText("0.00%");
        ((ImageView) view.findViewById(R.id.tab_trend_icon)).setImageResource(R.drawable.ic_trending_flat);

        return view;
    }

    public void updateTab(int position, ArrayList<History> records) {
        TabLayout.Tab tab = tabs.getTabAt(position);
        View view = tab.getCustomView();

        double curPrice = records.get(records.size() - 1).getClose();
        double prevPrice = records.get(0).getClose();
        double priceDiff = curPrice - prevPrice;

        String priceString = new PriceFormat(records.get(0).getToSymbol()).format(priceDiff);
        ((TextView) view.findViewById(R.id.tab_price)).setText(priceString);

        boolean isSelected = this.tab != null && this.tab.getPosition() == position;

        double trend = priceDiff / prevPrice;
        TextView trendView = view.findViewById(R.id.tab_trend);
        trendView.setText(new TrendValueFormat().format(trend));
        trendView.setTextColor(new TrendColorFormat().format(trend));

        ImageView icon = view.findViewById(R.id.tab_trend_icon);
        if (isSelected) {
            icon.setImageResource(IconHelper.getWhiteTrendIconResId(trend));
        } else {
            icon.setImageResource(IconHelper.getTrendIconResId(trend));
        }

        tab.setTag(priceDiff);
    }

    private void setSelected(int position) {
        View view = tab.getCustomView();
        view.findViewById(R.id.tab_container).setBackgroundColor(Color.WHITE);
        ((TextView) view.findViewById(R.id.tab_label)).setTextColor(Color.parseColor("#80000000"));
        ((TextView) view.findViewById(R.id.tab_price)).setTextColor(Color.parseColor("#80000000"));

        ((TextView) view.findViewById(R.id.tab_trend)).setTextColor(Color.parseColor("#80000000"));
        Object tag = tab.getTag();
        double priceDiff = tag == null ? 0 : (double) tag;
        ((ImageView) view.findViewById(R.id.tab_trend_icon))
                .setImageResource(IconHelper.getTrendIconResId(priceDiff));

        tab = tabs.getTabAt(position);
        view = tab.getCustomView();
        view.findViewById(R.id.tab_container).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        ((TextView) view.findViewById(R.id.tab_label)).setTextColor(Color.WHITE);
        ((TextView) view.findViewById(R.id.tab_price)).setTextColor(Color.WHITE);

        tag = tab.getTag();
        priceDiff = tag == null ? 0 : (double) tag;
        ((TextView) view.findViewById(R.id.tab_trend)).setTextColor(Color.WHITE);
        ((ImageView) view.findViewById(R.id.tab_trend_icon))
                .setImageResource(IconHelper.getWhiteTrendIconResId(priceDiff));
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
