package com.example.coinkarasu.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class CoinListRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;

    private OnItemClickListener listener;
    private LayoutInflater inflater;
    private ArrayList<Coin> coins = new ArrayList<>();
    private TreeSet<Integer> sectionHeader = new TreeSet<>();
    private boolean isAnimEnabled;
    private boolean isDownloadIconEnabled;
    private boolean isScrolled;
    private boolean isAnimPaused;

    private ResourceUtils resources;

    private FragmentManager fragmentManager;

    public CoinListRecyclerViewAdapter(Activity activity, List<Coin> coins, FragmentManager fragmentManager) {
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
    public int getItemCount() {
        return coins.size();
    }


    public ArrayList<Coin> getItems() {
        ArrayList<Coin> filtered = new ArrayList<>();
        for (Coin coin : coins) {
            if (coin.isSectionHeader()) {
                continue;
            }
            filtered.add(coin);
        }

        return filtered;
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


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return new HeaderViewHolder(inflater.inflate(R.layout.recycler_coin_list_header_item, parent, false));
            case TYPE_ITEM:
                return new ItemViewHolder(inflater.inflate(R.layout.recycler_coin_list_row_item, parent, false));
            default:
                throw new RuntimeException("Invalid viewType " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_HEADER:
                bindHeaderViewHolder((HeaderViewHolder) holder, position);
                break;
            case TYPE_ITEM:
                bindItemViewHolder((ItemViewHolder) holder, position);
                break;
        }
    }

    private void bindHeaderViewHolder(HeaderViewHolder holder, int position) {
        Coin coin = coins.get(position);

        holder.header.setText(resources.headerNameResIdStringMap.get(coin.getHeaderNameResId()));
        holder.setProgressbarTag(coin.getExchange());
        holder.initializeTimeSpan(fragmentManager, coin);

        if (position == 0) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }
    }

    private void bindItemViewHolder(final ItemViewHolder holder, int position) {
        final Coin coin = coins.get(position);

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
                    holder.priceBgColorAnimator = new PriceBgColorAnimator(resources.priceUpFromColor, resources.priceToColor, holder.container);
                } else {
                    holder.priceBgColorAnimator = new PriceBgColorAnimator(resources.priceDownFromColor, resources.priceToColor, holder.container);
                }
                holder.priceBgColorAnimator.start();
            } else {
                holder.price.setText(resources.priceFormatter.format(coin.getPrice()));
                holder.container.setBackgroundColor(resources.priceToColor);
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
            holder.container.setBackgroundColor(resources.priceToColor);
        }

        holder.trendIcon.setImageResource(resources.trendIconFormat.format(coin.getTrend()));

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(coin, view, holder.getAdapterPosition());
            }
        });
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        View container;
        NetworkImageView icon;
        TextView name;
        TextView symbol;
        TextView price;
        TextView price_diff;
        TextView trend;
        ImageView trendIcon;

        PriceAnimator priceAnimator = null;
        PriceDiffAnimator priceDiffAnimator = null;
        PriceBgColorAnimator priceBgColorAnimator = null;
        TrendAnimator trendAnimator = null;

        ItemViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            icon = view.findViewById(R.id.icon);
            name = view.findViewById(R.id.name);
            symbol = view.findViewById(R.id.symbol);
            price = view.findViewById(R.id.price);
            price_diff = view.findViewById(R.id.price_diff);
            trend = view.findViewById(R.id.trend);
            trendIcon = view.findViewById(R.id.trend_icon);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        View container;
        TextView header;
        View divider;
        View progressbar;
        View timeSpan;

        HeaderViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            header = view.findViewById(R.id.text_separator);
            divider = view.findViewById(R.id.divider);
            progressbar = view.findViewById(R.id.progressbar);
            timeSpan = view.findViewById(R.id.time_span_container);
        }

        void setProgressbarTag(String exchange) {
            progressbar.setTag(exchange + "-progressbar");
        }

        void initializeTimeSpan(FragmentManager manager, Coin coin) {
            timeSpan.setId(coin.getHeaderNameResId());
            manager.beginTransaction()
                    .replace(timeSpan.getId(), RelativeTimeSpanFragment.newInstance(), RelativeTimeSpanFragment.getTag(coin))
                    .commit();
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Coin coin, View view, int position);
    }
}
