package com.example.toolbartest;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.utils.LocaleHelper;
import com.example.toolbartest.utils.ResourceHelper;
import com.example.toolbartest.utils.StringHelper;
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
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_coin, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.coin_icon);
            holder.name = convertView.findViewById(R.id.coin_name);
            holder.symbol = convertView.findViewById(R.id.coin_symbol);
            holder.price = convertView.findViewById(R.id.coin_price);
            holder.trend = convertView.findViewById(R.id.coin_trend);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Coin coin = (Coin) getItem(position);

        if (ICON_READY_SYMBOLS.contains(coin.getSymbol())) {
            String name = "ic_coin_" + coin.getSymbol().toLowerCase();
            holder.icon.setDefaultImageResId(ResourceHelper.getDrawableResourceIdByName(activity, name));
            holder.icon.setImageUrl(null, VolleyHelper.getInstance(activity).getImageLoader());
        } else {
            String iconUrl = coin.getImageUrl();
            holder.icon.setDefaultImageResId(R.drawable.ic_coin_android);
            holder.icon.setErrorImageResId(R.drawable.ic_coin_android);
            holder.icon.setImageUrl(iconUrl, VolleyHelper.getInstance(activity).getImageLoader());
        }

        holder.name.setText(coin.getCoinName());
        holder.symbol.setText(coin.getSymbol());
        holder.price.setText(StringHelper.formatPrice(coin.getPrice(), coin.getToSymbol()));

        holder.trend.setText(StringHelper.formatTrend(coin.getTrend()));
        holder.trend.setTextColor(getColor(coin.getTrend()));

        return convertView;
    }

    public void setCoins(ArrayList<Coin> coins) {
        this.coins.clear();
        this.coins.addAll(coins);
    }

    private int getColor(double trend) {
        int color = activity.getResources().getColor(R.color.neutral_trend);

        if (trend > 0) {
            color = activity.getResources().getColor(R.color.green);
        } else if (trend < 0) {
            color = Color.RED;
        }

        return color;
    }

    private class ViewHolder {
        NetworkImageView icon;
        TextView name;
        TextView symbol;
        TextView price;
        TextView trend;
    }
}