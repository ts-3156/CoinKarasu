package com.example.coinkarasu.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.coinkarasu.R;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.format.PriceViewFormat;
import com.example.coinkarasu.format.TrendViewFormat;
import com.example.coinkarasu.utils.IconHelper;
import com.example.coinkarasu.utils.PrefHelper;
import com.example.coinkarasu.utils.VolleyHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class ListViewAdapter extends BaseAdapter {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;

    private HashMap<String, Integer> symbolIconResIdMap;
    private ImageLoader imageLoader;
    private LayoutInflater inflater;
    private ArrayList<Coin> coins = new ArrayList<>();
    private TreeSet<Integer> sectionHeader = new TreeSet<>();
    private boolean isAnimEnabled;
    private boolean isDownloadIconEnabled;
    private boolean isScrolled;

    public ListViewAdapter(Activity activity, List<Coin> coins) {
        symbolIconResIdMap = buildIconResIdMap(activity, coins);
        imageLoader = VolleyHelper.getInstance(activity).getImageLoader();
        isAnimEnabled = PrefHelper.isAnimEnabled(activity);
        isDownloadIconEnabled = PrefHelper.isDownloadIconEnabled(activity);
        isScrolled = false;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (Coin coin : coins) {
            addItem(coin);
        }
    }

    private HashMap<String, Integer> buildIconResIdMap(Activity activity, List<Coin> coins) {
        HashMap<String, Integer> map = new HashMap<>();
        Resources resources = activity.getResources();
        String packageName = activity.getPackageName();

        for (Coin coin : coins) {
            if (coin.isSectionHeader()) {
                continue;
            }
            String name = "ic_coin_" + coin.getSymbol().toLowerCase();
            int resId = resources.getIdentifier(name, "raw", packageName);
            map.put(coin.getSymbol(), resId);
        }

        return map;
    }

    public void setIsScrolled(boolean flag) {
        this.isScrolled = flag;
    }

    public void setAnimEnabled(boolean flag) {
        this.isAnimEnabled = flag;
    }

    public void setDownloadIconEnabled(boolean flag) {
        this.isDownloadIconEnabled = flag;
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

    public ArrayList<Coin> getItems(String exchange) {
        ArrayList<Coin> filtered = new ArrayList<>();
        for (Coin coin : coins) {
            if (coin.isSectionHeader()) {
                continue;
            }
            if (coin.getExchange().equals(exchange)) {
                filtered.add(coin);
            }
        }

        return filtered;
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
            holder.icon.setDefaultImageResId(symbolIconResIdMap.get(coin.getSymbol()));
            if (isDownloadIconEnabled) {
                holder.icon.setImageUrl(coin.getImageUrl(), imageLoader);
            } else {
                holder.icon.setImageUrl(null, imageLoader);
            }

            holder.name.setText(coin.getCoinName());
            holder.symbol.setText(coin.getSymbol());

            new PriceViewFormat(coin, (isAnimEnabled && !isScrolled)).format(holder.price);
            new TrendViewFormat(coin, (isAnimEnabled && !isScrolled)).format(holder.trend);

            holder.trend_icon.setImageResource(IconHelper.getTrendIconResId(coin));
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
