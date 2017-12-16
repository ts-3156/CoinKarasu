package com.example.coinkarasu.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.coinkarasu.R;
import com.example.coinkarasu.adapters.ViewPagerAdapter;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.coins.CoinImpl;
import com.example.coinkarasu.coins.SnapshotCoin;
import com.example.coinkarasu.cryptocompare.ClientImpl;
import com.example.coinkarasu.cryptocompare.data.CoinSnapshot;
import com.example.coinkarasu.cryptocompare.data.History;
import com.example.coinkarasu.format.PriceTabFormat;
import com.example.coinkarasu.format.TrendViewFormat;
import com.example.coinkarasu.tasks.GetCoinSnapshotTask;
import com.example.coinkarasu.utils.IconHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;


public class CoinExchangeFragment extends Fragment implements GetCoinSnapshotTask.Listener, ViewPager.OnPageChangeListener {

    private OnFragmentInteractionListener listener;

    private String kind;
    private Coin coin;
    private boolean taskStarted;
    private int errorCount = 0;

    private boolean tabsCreated = false;
    private ViewPager pager;
    private TabLayout tabs;
    private TabLayout.Tab tab;

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
                coin = CoinImpl.buildByJSONObject(new JSONObject(coinJson));
            } catch (JSONException e) {
                Log.e("onCreate", e.getMessage());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_exchange, container, false);

        view.findViewById(R.id.popup_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(getContext(), view);
                popup.inflate(R.menu.coin_card);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.action_settings) {
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf");
        ((TextView) view.findViewById(R.id.caption_left)).setTypeface(typeFace);
        ((TextView) view.findViewById(R.id.caption_right)).setTypeface(typeFace);
        ((TextView) view.findViewById(R.id.caption_desc_left)).setTypeface(typeFace);

        typeFace = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-LightItalic.ttf");
        ((TextView) view.findViewById(R.id.caption_desc_right)).setTypeface(typeFace);

        ((TextView) view.findViewById(R.id.caption_left)).setText(getString(R.string.caption_left, coin.getSymbol(), coin.getToSymbol()));

        startTask();

        return view;
    }

    private void createTabs(ArrayList<SnapshotCoin> coins) {
        if (tabsCreated || getView() == null || getActivity() == null) {
            return;
        }
        tabsCreated = true;

        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        View view = getView();
        String fromSymbol = coin.getSymbol();
        String toSymbol = coin.getToSymbol();

        for (int i = 0; i < coins.size(); i++) {
            SnapshotCoin coin = coins.get(i);
            adapter.addItem(CoinExchangeTabContentFragment.newInstance(coin, fromSymbol, toSymbol, i, coin.getMarket().toLowerCase()));
        }

        pager = view.findViewById(R.id.view_pager);
        pager.setAdapter(adapter);
        pager.setCurrentItem(0);
        pager.addOnPageChangeListener(this);
        pager.setOffscreenPageLimit(Math.min(coins.size(), 5));

        tabs = view.findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(pager);

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < coins.size(); i++) {
            SnapshotCoin coin = coins.get(i);
            tabs.getTabAt(i).setCustomView(createTab(inflater, null, coin));
        }

        tab = tabs.getTabAt(0);
        setSelected(0);
    }

    private View createTab(LayoutInflater inflater, ViewGroup container, SnapshotCoin coin) {
        View view = inflater.inflate(R.layout.tab_exchange, container, false);

        ((TextView) view.findViewById(R.id.tab_label)).setText(coin.getMarket());

        String priceString = new PriceTabFormat(coin.getToSymbol()).format(coin.getPrice());
        ((TextView) view.findViewById(R.id.tab_price)).setText(priceString);

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

//        String priceString = new PriceTabFormat(records.get(0).getToSymbol()).format(priceDiff);
//        ((TextView) view.findViewById(R.id.tab_price)).setText(priceString);

        boolean isSelected = this.tab != null && this.tab.getPosition() == position;
        double trend = priceDiff / prevPrice;
        new TrendViewFormat(String.valueOf(trend))
                .format((TextView) view.findViewById(R.id.tab_trend), !isSelected);

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

    private void startTask() {
        if (taskStarted || errorCount >= 3) {
            return;
        }
        taskStarted = true;

        new GetCoinSnapshotTask(new ClientImpl(getActivity()))
                .setFromSymbol(coin.getSymbol())
                .setToSymbol(coin.getToSymbol())
                .setListener(this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void finished(CoinSnapshot snapshot) {
        ArrayList<SnapshotCoin> coins = snapshot.getSnapshotCoins();
        if (coins == null) {
            Log.e("finished", "null(retry), " + kind + ", " + errorCount);
            taskStarted = false;
            errorCount++;
            startTask();
            return;
        }

        if (coins.isEmpty()) {
            Log.e("finished", "empty, " + kind + ", " + errorCount);
            return;
        }

        if (isDetached() || getView() == null) {
            return;
        }

        Iterator<SnapshotCoin> iterator = coins.iterator();
        while (iterator.hasNext()) {
            SnapshotCoin coin = iterator.next();
            if (coin.getVolume24Hour() <= 0.0 || coin.getMarket().equals("LocalBitcoins")) {
                iterator.remove();
            }
        }

        Collections.sort(coins, new Comparator<SnapshotCoin>() {
            public int compare(SnapshotCoin c1, SnapshotCoin c2) {
                return c1.getPrice() > c2.getPrice() ? -1 : 1;
            }
        });

        createTabs(coins);
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
                CoinExchangeTabContentFragment fragment = (CoinExchangeTabContentFragment) adapter.getItem(position);

                setSelected(position);
                fragment.updateView();
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onLineChartKindChanged(String kind);
    }
}
