package com.example.toolbartest.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.example.toolbartest.R;
import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.utils.AnimHelper;
import com.example.toolbartest.utils.CoinPriceFormat;
import com.example.toolbartest.utils.ResourceHelper;
import com.example.toolbartest.utils.StringHelper;
import com.example.toolbartest.utils.VolleyHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

public class CustomAdapter extends BaseAdapter {
    private static final List<String> ICON_READY_SYMBOLS =
            Arrays.asList("BCH", "BTC", "ETC", "ETH", "LTC", "MONA", "REP", "XEM", "XMR", "XRP", "ZEC");

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;

    private Activity activity;
    private LayoutInflater inflater;

    private ArrayList<Coin> coins = new ArrayList<>();
    private TreeSet<Integer> sectionHeader = new TreeSet<>();
    private boolean isAnimEnabled;

    public CustomAdapter(Activity activity, List<Coin> coins) {
        this.activity = activity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        isAnimEnabled = true;
        for (Coin coin : coins) {
            addItem(coin);
        }
    }

    public void setAnimEnabled(boolean flag) {
        this.isAnimEnabled = flag;
    }

    public void addItem(Coin coin) {
        coins.add(coin);
        if (coin.isSectionHeader()) {
            sectionHeader.add(coins.size() - 1);
        }
        notifyDataSetChanged();
    }

    public void replaceItems(List<Coin> coins) {
        this.coins.clear();
        this.coins = new ArrayList<>();

        sectionHeader.clear();
        sectionHeader = new TreeSet<>();

        for (Coin coin : coins) {
            addItem(coin);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return sectionHeader.contains(position) ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return coins.size();
    }

    @Override
    public Coin getItem(int position) {
        return coins.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int rowType = getItemViewType(position);
        Coin coin = getItem(position);

        if (convertView == null) {
            holder = new ViewHolder();

            if (rowType == TYPE_ITEM) {
                convertView = inflater.inflate(R.layout.list_row_item, parent, false);
                holder.icon = convertView.findViewById(R.id.coin_icon);
                holder.name = convertView.findViewById(R.id.coin_name);
                holder.symbol = convertView.findViewById(R.id.coin_symbol);
                holder.price = convertView.findViewById(R.id.coin_price);
                holder.trend = convertView.findViewById(R.id.coin_trend);
                holder.trend_icon = convertView.findViewById(R.id.coin_trend_icon);
            } else if (rowType == TYPE_HEADER) {
                convertView = inflater.inflate(R.layout.list_header_item, parent, false);
                holder.header = convertView.findViewById(R.id.text_separator);
                holder.divider = convertView.findViewById(R.id.divider);
                holder.progressbar = convertView.findViewById(R.id.progressbar);

                holder.progressbar.setTag(coin.getExchange());
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (rowType == TYPE_ITEM) {
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

            if (isAnimEnabled) {
                AnimHelper.setPriceAnim(holder.price, coin);
                AnimHelper.setTrendAnim(activity, holder.trend, coin);
                AnimHelper.setTrendIcon(holder.trend_icon, coin);
            } else {
                holder.price.setText(new CoinPriceFormat(coin.getToSymbol()).format(coin.getPrice()));

                holder.trend.setText(StringHelper.formatTrend(coin.getTrend()));
                holder.trend.setTextColor(AnimHelper.getTrendColor(activity, coin.getTrend()));
                AnimHelper.setTrendIcon(holder.trend_icon, coin);
            }
        } else if (rowType == TYPE_HEADER) {
            holder.header.setText(coin.getName());
            if (position == 0) {
                holder.divider.setVisibility(View.GONE);
            } else {
                holder.divider.setVisibility(View.VISIBLE);
            }
        }

        return convertView;
    }

    private class ViewHolder {
        NetworkImageView icon;
        TextView name;
        TextView symbol;
        TextView price;
        TextView trend;
        ImageView trend_icon;

        TextView header;
        View divider;
        View progressbar;
    }
}
