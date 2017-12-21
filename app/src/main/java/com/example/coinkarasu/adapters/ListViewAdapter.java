package com.example.coinkarasu.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.example.coinkarasu.R;
import com.example.coinkarasu.activities.RelativeTimeSpanFragment;
import com.example.coinkarasu.animator.PriceAnimator;
import com.example.coinkarasu.animator.PriceBgColorAnimator;
import com.example.coinkarasu.animator.PriceDiffAnimator;
import com.example.coinkarasu.animator.TrendAnimator;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.utils.PrefHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class ListViewAdapter extends BaseAdapter {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;

    private LayoutInflater inflater;
    private ArrayList<Coin> coins = new ArrayList<>();
    private TreeSet<Integer> sectionHeader = new TreeSet<>();
    private boolean isAnimEnabled;
    private boolean isDownloadIconEnabled;
    private boolean isScrolled;
    private boolean isAnimPaused;

    private ResourceUtils resources;

    private FragmentManager fragmentManager;

    public ListViewAdapter(Activity activity, List<Coin> coins, FragmentManager fragmentManager) {
        isAnimEnabled = PrefHelper.isAnimEnabled(activity);
        isDownloadIconEnabled = PrefHelper.isDownloadIconEnabled(activity);
        isScrolled = false;
        isAnimPaused = false;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (Coin coin : coins) {
            addItem(coin);
        }

        resources = new ResourceUtils(activity, coins);

        this.fragmentManager = fragmentManager;
    }

    public void setIsScrolled(boolean flag) {
        this.isScrolled = flag;
    }

    public void setAnimEnabled(boolean flag) {
        this.isAnimEnabled = flag;
    }

    public void setToSymbol(String symbol) {
        for (Coin coin : coins) {
            coin.setToSymbol(symbol);
        }
        resources.toSymbolChanged(symbol);
    }

    public void setDownloadIconEnabled(boolean flag) {
        this.isDownloadIconEnabled = flag;
    }

    public void pauseAnimation() {
        isAnimPaused = true;
    }

    public void restartAnimation() {
        isAnimPaused = false;
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

        notifyDataSetChanged();

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
                holder.icon = convertView.findViewById(R.id.icon);
                holder.name = convertView.findViewById(R.id.name);
                holder.symbol = convertView.findViewById(R.id.symbol);
                holder.price = convertView.findViewById(R.id.price);
                holder.price_diff = convertView.findViewById(R.id.price_diff);
                holder.trend = convertView.findViewById(R.id.trend);
                holder.trendIcon = convertView.findViewById(R.id.trend_icon);

                holder.name.setTypeface(resources.typeFace);
                holder.symbol.setTypeface(resources.typeFace);
            } else if (rowType == TYPE_HEADER) {
                convertView = inflater.inflate(R.layout.list_header_item, parent, false);
                holder.header = convertView.findViewById(R.id.text_separator);
                holder.divider = convertView.findViewById(R.id.divider);

                holder.header.setTypeface(resources.typeFaceItalic);

                holder.progressbar = convertView.findViewById(R.id.progressbar);
                holder.progressbar.setTag(coin.getExchange() + "-progressbar");

                View timeSpan = convertView.findViewById(R.id.time_span_container);
                timeSpan.setId(Math.abs(coin.getExchange().hashCode()));
                fragmentManager.beginTransaction()
                        .replace(timeSpan.getId(), RelativeTimeSpanFragment.newInstance(), RelativeTimeSpanFragment.getTag(coin.getExchange()))
                        .commit();
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (rowType == TYPE_ITEM) {
            holder.icon.setDefaultImageResId(resources.symbolIconResIdMap.get(coin.getSymbol()));
            if (isDownloadIconEnabled) {
                holder.icon.setImageUrl(coin.getFullImageUrl(), resources.imageLoader);
            } else {
                holder.icon.setImageUrl(null, resources.imageLoader);
            }

            holder.name.setText(coin.getCoinName());
            holder.symbol.setText(coin.getSymbol());
            holder.price_diff.setTextColor(resources.getPriceColor(coin.getPriceDiff()));
            holder.trend.setTextColor(resources.getTrendColor(coin.getTrend()));

            if (holder.priceAnimator != null) {
                holder.priceAnimator.cancel();
                holder.priceAnimator = null;
            }
            if (holder.priceDiffAnimator != null) {
                holder.priceDiffAnimator.cancel();
                holder.priceDiffAnimator = null;
            }
            if (holder.priceBgColorAnimator != null) {
                holder.priceBgColorAnimator.cancel();
                holder.priceBgColorAnimator = null;
            }
            if (holder.trendAnimator != null) {
                holder.trendAnimator.cancel();
                holder.trendAnimator = null;
            }

            if (!isAnimPaused && isAnimEnabled && !isScrolled) {
                if (coin.getPrice() != coin.getPrevPrice()) {
                    holder.priceAnimator = new PriceAnimator(coin, holder.price);
                    holder.priceAnimator.start();

                    if (coin.getPrice() > coin.getPrevPrice()) {
                        holder.priceBgColorAnimator = new PriceBgColorAnimator(resources.priceUpFromColor, resources.priceToColor, convertView);
                    } else {
                        holder.priceBgColorAnimator = new PriceBgColorAnimator(resources.priceDownFromColor, resources.priceToColor, convertView);
                    }
                    holder.priceBgColorAnimator.start();
                } else {
                    holder.price.setText(resources.priceFormatter.format(coin.getPrice()));
                    convertView.setBackgroundColor(resources.priceToColor);
                }

                if (coin.getPriceDiff() != coin.getPrevPriceDiff()) {
                    holder.priceDiffAnimator = new PriceDiffAnimator(coin, holder.price_diff);
                    holder.priceDiffAnimator.start();
                } else {
                    holder.price_diff.setText(resources.signedPriceFormatter.format(coin.getPriceDiff()));
                }

                if (coin.getTrend() != coin.getPrevTrend()) {
                    holder.trendAnimator = new TrendAnimator(coin, holder.trend);
                    holder.trendAnimator.start();
                } else {
                    holder.trend.setText(resources.surroundedTrendFormatter.format(coin.getTrend()));
                }
            } else {
                holder.price.setText(resources.priceFormatter.format(coin.getPrice()));
                holder.price_diff.setText(resources.signedPriceFormatter.format(coin.getPriceDiff()));
                holder.trend.setText(resources.surroundedTrendFormatter.format(coin.getTrend()));
                convertView.setBackgroundColor(resources.priceToColor);
            }

            holder.trendIcon.setImageResource(resources.trendIconFormat.format(coin.getTrend()));
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

    private static class ViewHolder {
        NetworkImageView icon;
        TextView name;
        TextView symbol;
        TextView price;
        TextView price_diff;
        TextView trend;
        ImageView trendIcon;

        TextView header;
        View divider;
        View progressbar;

        PriceAnimator priceAnimator = null;
        PriceDiffAnimator priceDiffAnimator = null;
        PriceBgColorAnimator priceBgColorAnimator = null;
        TrendAnimator trendAnimator = null;
    }
}
