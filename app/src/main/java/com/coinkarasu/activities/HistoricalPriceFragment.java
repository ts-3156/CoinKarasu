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
import com.coinkarasu.utils.CKLog;

import java.util.List;


public class HistoricalPriceFragment extends Fragment implements
        ViewPager.OnPageChangeListener {

    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "HistoricalPriceFragment";
    private static final HistoricalPriceKind DEFAULT_KIND = HistoricalPriceKind.hour;

    private String fromSymbol;
    private String toSymbol;
    private TabLayout.Tab tab;
    private ViewPager pager;
    private TabLayout tabs;

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

        pager = view.findViewById(R.id.view_pager);
        pager.setAdapter(new HistoricalPricePagerAdapter(getChildFragmentManager(), fromSymbol, toSymbol));
        pager.setCurrentItem(DEFAULT_KIND.ordinal());
        pager.addOnPageChangeListener(this);
        pager.setOffscreenPageLimit(HistoricalPriceKind.values().length);

        tabs = view.findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(pager);

        for (HistoricalPriceKind kind : HistoricalPriceKind.values()) {
            TabViewHolder holder = new TabViewHolder(inflater.inflate(R.layout.tab_historical_price, null, false), getString(kind.labelResId));
            TabLayout.Tab tab = tabs.getTabAt(kind.ordinal());
            tab.setCustomView(holder.itemView);
            tab.setTag(holder);
        }

        tab = tabs.getTabAt(DEFAULT_KIND.ordinal());
        setTabSelected(tab);

        Spanned text = Html.fromHtml(getString(R.string.line_chart_info, fromSymbol, toSymbol));
        ((TextView) view.findViewById(R.id.info_text)).setText(text);

        return view;
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

        holder.price.setText(new SignedPriceFormat(records.get(0).getToSymbol()).format(holder.priceDiff));
        holder.price.setTextColor(getResources().getColor(new PriceColorFormat().format(holder.priceDiff, isSelected)));
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
        holder.price.setTextColor(getResources().getColor(new PriceColorFormat().format(holder.priceDiff)));
        holder.trend.setTextColor(getResources().getColor(new TrendColorFormat().format(holder.priceDiff)));
        holder.trendIcon.setImageResource(new TrendIconFormat().format(holder.priceDiff));
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
        TextView price;
        TextView trend;
        ImageView trendIcon;
        double priceDiff;

        TabViewHolder(View itemView, String labelString) {
            this.itemView = itemView;
            container = itemView.findViewById(R.id.tab_container);
            label = itemView.findViewById(R.id.label);
            price = itemView.findViewById(R.id.price);
            trend = itemView.findViewById(R.id.trend);
            trendIcon = itemView.findViewById(R.id.trend_icon);
            priceDiff = 0.0;

            label.setText(labelString);
        }
    }
}
