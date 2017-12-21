package com.example.coinkarasu.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
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

import com.example.coinkarasu.R;
import com.example.coinkarasu.adapters.CoinLineChartPagerAdapter;
import com.example.coinkarasu.cryptocompare.data.History;
import com.example.coinkarasu.format.PriceFormat;
import com.example.coinkarasu.format.TrendColorFormat;
import com.example.coinkarasu.format.TrendIconFormat;
import com.example.coinkarasu.format.TrendValueFormat;

import java.util.ArrayList;


public class CoinLineChartFragment extends Fragment implements
        ViewPager.OnPageChangeListener {

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

    private String fromSymbol;
    private String toSymbol;
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

        ViewPager pager = view.findViewById(R.id.view_pager);
        pager.setAdapter(new CoinLineChartPagerAdapter(getChildFragmentManager(), fromSymbol, toSymbol));
        pager.setCurrentItem(DEFAULT_KIND.ordinal());
        pager.addOnPageChangeListener(this);
        pager.setOffscreenPageLimit(Kind.values().length);

        TabLayout tabs = view.findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(pager);

        tabs.getTabAt(Kind.hour.ordinal()).setCustomView(createTab(inflater, container, Kind.hour.label));
        tabs.getTabAt(Kind.day.ordinal()).setCustomView(createTab(inflater, container, Kind.day.label));
        tabs.getTabAt(Kind.week.ordinal()).setCustomView(createTab(inflater, container, Kind.week.label));
        tabs.getTabAt(Kind.month.ordinal()).setCustomView(createTab(inflater, container, Kind.month.label));
        tabs.getTabAt(Kind.year.ordinal()).setCustomView(createTab(inflater, container, Kind.year.label));

        tab = tabs.getTabAt(DEFAULT_KIND.ordinal());
        setSelected(DEFAULT_KIND.ordinal(), view);

        Spanned text = Html.fromHtml(getString(R.string.line_chart_info, fromSymbol, toSymbol));
        ((TextView) view.findViewById(R.id.info_text)).setText(text);

        return view;
    }

    private View createTab(LayoutInflater inflater, ViewGroup container, String label) {
        View view = inflater.inflate(R.layout.tab_line_chart, container, false);

        ((TextView) view.findViewById(R.id.label)).setText(label);
        ((TextView) view.findViewById(R.id.price)).setText("0.00");
        ((TextView) view.findViewById(R.id.trend)).setText("0.00%");
        ((ImageView) view.findViewById(R.id.trend_icon)).setImageResource(R.drawable.ic_trending_flat);

        return view;
    }

    public void updateTab(int position, ArrayList<History> records) {
        View container = getView();
        if (container == null) {
            return;
        }

        TabLayout tabs = getView().findViewById(R.id.tab_layout);
        if (tabs == null) {
            return;
        }

        TabLayout.Tab tab = tabs.getTabAt(position);
        View view = tab.getCustomView();

        double curPrice = records.get(records.size() - 1).getClose();
        double prevPrice = records.get(0).getClose();
        double priceDiff = curPrice - prevPrice;

        String priceString = new PriceFormat(records.get(0).getToSymbol()).format(priceDiff);
        ((TextView) view.findViewById(R.id.price)).setText(priceString);

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fromSymbol = null;
        toSymbol = null;
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
