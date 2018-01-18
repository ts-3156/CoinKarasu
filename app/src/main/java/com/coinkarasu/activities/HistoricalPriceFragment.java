package com.coinkarasu.activities;

import android.graphics.Color;
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
import com.coinkarasu.activities.etc.HistoricalPriceKind;
import com.coinkarasu.api.cryptocompare.data.History;
import com.coinkarasu.format.PriceColorFormat;
import com.coinkarasu.format.SignedPriceFormat;
import com.coinkarasu.format.TrendColorFormat;
import com.coinkarasu.format.TrendIconFormat;
import com.coinkarasu.format.TrendValueFormat;
import com.coinkarasu.pagers.HistoricalPricePagerAdapter;

import java.util.List;


public class HistoricalPriceFragment extends Fragment implements
        ViewPager.OnPageChangeListener {

    private static final boolean DEBUG = true;
    private static final String TAG = "HistoricalPriceFragment";
    private static final HistoricalPriceKind DEFAULT_KIND = HistoricalPriceKind.hour;

    private String fromSymbol;
    private String toSymbol;
    private TabLayout.Tab tab;

    public HistoricalPriceFragment() {
    }

    public static HistoricalPriceFragment newInstance(String fromSymbol, String toSymbol) {
        HistoricalPriceFragment fragment = new HistoricalPriceFragment();
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
        View view = inflater.inflate(R.layout.fragment_histrical_price, container, false);

        ((TextView) view.findViewById(R.id.caption_left)).setText(getString(R.string.caption_left, fromSymbol, toSymbol));

        ViewPager pager = view.findViewById(R.id.view_pager);
        pager.setAdapter(new HistoricalPricePagerAdapter(getChildFragmentManager(), fromSymbol, toSymbol));
        pager.setCurrentItem(DEFAULT_KIND.ordinal());
        pager.addOnPageChangeListener(this);
        pager.setOffscreenPageLimit(HistoricalPriceKind.values().length);

        TabLayout tabs = view.findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(pager);

        for (HistoricalPriceKind kind : HistoricalPriceKind.values()) {
            tabs.getTabAt(kind.ordinal()).setCustomView(createTab(inflater, container, getString(kind.labelResId)));
        }

        tab = tabs.getTabAt(DEFAULT_KIND.ordinal());
        setTabSelected(tab);

        Spanned text = Html.fromHtml(getString(R.string.line_chart_info, fromSymbol, toSymbol));
        ((TextView) view.findViewById(R.id.info_text)).setText(text);

        return view;
    }

    private View createTab(LayoutInflater inflater, ViewGroup container, String label) {
        View view = inflater.inflate(R.layout.tab_historical_price, container, false);

        ((TextView) view.findViewById(R.id.label)).setText(label);
        ((TextView) view.findViewById(R.id.price)).setText("0.00");
        ((TextView) view.findViewById(R.id.trend)).setText("0.00%");
        ((ImageView) view.findViewById(R.id.trend_icon)).setImageResource(R.drawable.ic_trending_flat);

        return view;
    }

    public void updateTab(int position, List<History> records) {
        if (getView() == null) {
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
        boolean isSelected = this.tab != null && this.tab.getPosition() == position;

        TextView priceView = view.findViewById(R.id.price);
        priceView.setText(new SignedPriceFormat(records.get(0).getToSymbol()).format(priceDiff));
        priceView.setTextColor(getResources().getColor(new PriceColorFormat().format(priceDiff, isSelected)));

        double trend = priceDiff / prevPrice;
        TextView trendView = view.findViewById(R.id.trend);
        trendView.setText(new TrendValueFormat().format(trend));
        trendView.setTextColor(getResources().getColor(new TrendColorFormat().format(trend, isSelected)));

        ImageView icon = view.findViewById(R.id.trend_icon);
        icon.setImageResource(new TrendIconFormat().format(trend, isSelected));

        tab.setTag(priceDiff);
    }

    private void setTabSelected(TabLayout.Tab tab) {
        if (tab == null || tab.getCustomView() == null) {
            return;
        }

        View view = tab.getCustomView();
        int activeTextColor = getResources().getColor(R.color.colorTabActiveText);

        view.findViewById(R.id.tab_container).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        ((TextView) view.findViewById(R.id.label)).setTextColor(activeTextColor);
        ((TextView) view.findViewById(R.id.price)).setTextColor(activeTextColor);
        ((TextView) view.findViewById(R.id.trend)).setTextColor(activeTextColor);

        Object tag = tab.getTag();
        double priceDiff = tag == null ? 0 : (double) tag;

        ((ImageView) view.findViewById(R.id.trend_icon))
                .setImageResource(new TrendIconFormat().format(priceDiff, true));
    }

    private void setTabUnselected(TabLayout.Tab tab) {
        if (tab == null || tab.getCustomView() == null) {
            return;
        }

        View view = tab.getCustomView();
        int inactiveTextColor = getResources().getColor(R.color.colorTabInactiveText);

        view.findViewById(R.id.tab_container).setBackgroundColor(Color.WHITE);
        ((TextView) view.findViewById(R.id.label)).setTextColor(inactiveTextColor);

        Object tag = tab.getTag();
        double priceDiff = tag == null ? 0 : (double) tag;

        ((TextView) view.findViewById(R.id.price))
                .setTextColor(getResources().getColor(new PriceColorFormat().format(priceDiff)));
        ((TextView) view.findViewById(R.id.trend))
                .setTextColor(getResources().getColor(new TrendColorFormat().format(priceDiff)));
        ((ImageView) view.findViewById(R.id.trend_icon))
                .setImageResource(new TrendIconFormat().format(priceDiff));
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
                setTabUnselected(tab);
                tab = ((TabLayout) getView().findViewById(R.id.tab_layout)).getTabAt(position);
                setTabSelected(tab);
            }
        }
    }
}
