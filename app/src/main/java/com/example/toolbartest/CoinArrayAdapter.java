package com.example.toolbartest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

public class CoinArrayAdapter extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater = null;
    ArrayList<Coin> coins;

    public CoinArrayAdapter(Context context, ArrayList<Coin> coins) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.coins = coins;
    }

    public void setCoins(ArrayList<Coin> coins) {
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
        convertView = layoutInflater.inflate(R.layout.list_coin, parent, false);
        Coin coin = coins.get(position);

        ((ImageView) convertView.findViewById(R.id.coin_icon)).setImageBitmap(coin.getIcon());
        ((TextView) convertView.findViewById(R.id.coin_name)).setText(coin.getName());
        ((TextView) convertView.findViewById(R.id.coin_symbol)).setText(String.valueOf(coin.getSymbol()));
        ((TextView) convertView.findViewById(R.id.coin_price)).setText(formatPrice(coin.getPrice(), Locale.JAPAN));
        ((TextView) convertView.findViewById(R.id.coin_trend)).setText(formatTrend(coin.getTrend()));

        return convertView;
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