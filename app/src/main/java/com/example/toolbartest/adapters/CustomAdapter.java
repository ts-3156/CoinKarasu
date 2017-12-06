package com.example.toolbartest.adapters;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.example.toolbartest.R;
import com.example.toolbartest.coins.Coin;
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

    public CustomAdapter(Activity activity) {
        this.activity = activity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public CustomAdapter(Activity activity, ArrayList<Coin> coins) {
        this.activity = activity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (Coin coin : coins) {
            addItem(coin);
        }
    }

    public void addItem(Coin coin) {
        coins.add(coin);
        if (coin.isSectionHeader()) {
            sectionHeader.add(coins.size() - 1);
        }
        notifyDataSetChanged();
    }

    public void replaceItems(ArrayList<Coin> coins) {
        this.coins = new ArrayList<>();
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

        if (convertView == null) {
            holder = new ViewHolder();

            if (rowType == TYPE_ITEM) {
                convertView = inflater.inflate(R.layout.list_row_item, parent, false);
                holder.icon = convertView.findViewById(R.id.coin_icon);
                holder.name = convertView.findViewById(R.id.coin_name);
                holder.symbol = convertView.findViewById(R.id.coin_symbol);
                holder.price = convertView.findViewById(R.id.coin_price);
                holder.trend = convertView.findViewById(R.id.coin_trend);
            } else if (rowType == TYPE_HEADER) {
                convertView = inflater.inflate(R.layout.list_header_item, null);
                holder.name = convertView.findViewById(R.id.text_separator);
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Coin coin = getItem(position);

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

            setPriceAnim(holder.price, coin);
            setTrendAnim(holder.trend, coin);
        } else if (rowType == TYPE_HEADER) {
            holder.name.setText(coin.getName());
        }


        return convertView;
    }

    private void setPriceAnim(final TextView view, final Coin coin) {
        double prev = coin.getPrevPrice();
        if (prev == 0.0) {
            prev = 0.95 * coin.getPrice();
        }

        ValueAnimator animator = ValueAnimator.ofFloat((float) prev, (float) coin.getPrice());
        animator.setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                String text = StringHelper.formatPrice(Double.valueOf(animation.getAnimatedValue().toString()), coin.getToSymbol());
                view.setText(text);
            }
        });
        animator.start();
    }

    private void setTrendAnim(final TextView view, final Coin coin) {
        view.setTextColor(getTrendColor(coin.getTrend()));

        double prev = coin.getPrevTrend();
        if (prev == 0.0) {
            prev = 0.95 * coin.getTrend();
        }

        ValueAnimator animator = ValueAnimator.ofFloat((float) prev, (float) coin.getTrend());
        animator.setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                String text = StringHelper.formatTrend(Double.valueOf(animation.getAnimatedValue().toString()));
                view.setText(text);
            }
        });
        animator.start();
    }

    private int getTrendColor(double trend) {
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
