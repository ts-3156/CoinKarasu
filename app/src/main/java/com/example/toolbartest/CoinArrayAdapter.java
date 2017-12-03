package com.example.toolbartest;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.utils.ResourceHelper;
import com.example.toolbartest.utils.VolleyHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class CoinArrayAdapter extends BaseAdapter {
    private static final List<String> ICON_READY_SYMBOLS =
            Arrays.asList("BCH", "BTC", "ETC", "ETH", "LTC", "MONA", "REP", "XEM", "XMR", "XRP", "ZEC");

    Activity activity;
    LayoutInflater layoutInflater;
    ArrayList<Coin> coins;

    public CoinArrayAdapter(Activity activity, ArrayList<Coin> coins) {
        this.activity = activity;
        this.layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.coins = coins;
    }

    @Override
    public int getCount() {
        return coins.size();
    }

    @Override
    public Object getItem(int position) {
        return coins.get(position);
    }

    @Override
    public long getItemId(int position) {
        return coins.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.list_coin, parent, false);
        }

        Coin coin = (Coin) getItem(position);
        NetworkImageView image = view.findViewById(R.id.coin_icon);

        if (ICON_READY_SYMBOLS.contains(coin.getSymbol())) {
            String name = "ic_coin_" + coin.getSymbol().toLowerCase();
            image.setDefaultImageResId(ResourceHelper.getDrawableResourceIdByName(activity, name));
        } else {
            String iconUrl = coin.getImageUrl();
            image.setDefaultImageResId(R.drawable.ic_coin_android);
            image.setErrorImageResId(R.drawable.ic_coin_android);
            image.setImageUrl(iconUrl, VolleyHelper.getInstance(activity.getApplicationContext()).getImageLoader());
        }

        ((TextView) view.findViewById(R.id.coin_name)).setText(coin.getCoinName());
        ((TextView) view.findViewById(R.id.coin_symbol)).setText(coin.getSymbol());
        ((TextView) view.findViewById(R.id.coin_price)).setText(formatPrice(coin.getPrice(), Locale.JAPAN));
        ((TextView) view.findViewById(R.id.coin_trend)).setText(formatTrend(coin.getTrend()));

        return view;
    }

    public void setCoins(ArrayList<Coin> coins) {
        this.coins.clear();
        this.coins.addAll(coins);
    }

    private String formatPrice(double price, Locale locale) {
        Currency currency = Currency.getInstance(Currency.getInstance(locale).getCurrencyCode());
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        return formatter.format(price / Math.pow(10, currency.getDefaultFractionDigits()));
    }

    private String formatTrend(double trend) {
        NumberFormat formatter = NumberFormat.getPercentInstance();
        formatter.setMinimumFractionDigits(2);
        return formatter.format(trend);
    }
}