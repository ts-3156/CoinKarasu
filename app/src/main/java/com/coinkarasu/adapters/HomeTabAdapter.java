package com.coinkarasu.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.coinkarasu.R;
import com.coinkarasu.activities.etc.TrendingKind;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.custom.NetworkSparkView;

import java.util.ArrayList;
import java.util.List;

public class HomeTabAdapter extends RecyclerView.Adapter<HomeTabAdapter.ViewHolder> {
    private OnItemClickListener listener;
    private List<Coin> coins;
    private List<Coin> visibleCoins;
    private ResourceUtils resources;
    private Configurations configs;
    private TrendingKind kind;

    public HomeTabAdapter(Context context, List<Coin> coins, boolean isFiltered) {
        this.coins = coins;
        setFilterOnlyTrending(isFiltered);

        resources = new ResourceUtils(context, coins);
        configs = new Configurations(context);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.home_tab_row_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Coin coin = visibleCoins.get(position);

        holder.symbol.setText(coin.getSymbol());
        holder.price.setText(resources.getPriceFormatter(coin.getToSymbol()).format(coin.getPrice()));
        holder.trend.setText(resources.trendFormatter.format(coin.getTrend()));
        holder.trend.setTextColor(resources.getTrendColor(coin.getTrend()));

        holder.icon.setDefaultImageResId(resources.symbolIconResIdMap.get(coin.getSymbol()));
        holder.icon.setImageUrl(coin.getLargeImageUrl(), resources.imageLoader);

        // if (kind != null && kind == TrendingKind.one_hour) {
        //     holder.trendIcon.setVisibility(View.GONE);
        //     holder.sparkLine.setVisibility(View.VISIBLE);
        //     holder.sparkLine.setConfigurations(configs);
        //     holder.sparkLine.setKind(HistoricalPriceKind.hour);
        //     holder.sparkLine.setSymbols(coin.getSymbol(), coin.getToSymbol());
        // } else {
        holder.trendIcon.setVisibility(View.VISIBLE);
        holder.sparkLine.setVisibility(View.GONE);
        holder.trendIcon.setImageResource(resources.trendIconFormat.format(coin.getTrend()));
        // }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(coin, view, holder.getAdapterPosition());
            }
        });
    }

    public void onViewRecycled(ViewHolder holder) {
        holder.sparkLine.clearData();
        holder.icon.setImageUrl(null, null);

        holder.container.setOnClickListener(null);
    }

    @Override
    public int getItemCount() {
        return visibleCoins.size();
    }

    public void setTrendingKind(TrendingKind kind) {
        this.kind = kind;
    }

    public void setFilterOnlyTrending(boolean isFiltered) {
        if (isFiltered) {
            List<Coin> filtered = new ArrayList<>();
            for (Coin coin : coins) {
                if (coin.getTrend() > 0.0) {
                    filtered.add(coin);
                }
            }
            visibleCoins = filtered;
        } else {
            visibleCoins = coins;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View container;
        NetworkImageView icon;
        TextView symbol;
        TextView price;
        TextView trend;
        ImageView trendIcon;
        NetworkSparkView sparkLine;

        ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            icon = view.findViewById(R.id.icon);
            symbol = view.findViewById(R.id.symbol);
            price = view.findViewById(R.id.price);
            trend = view.findViewById(R.id.trend);
            trendIcon = view.findViewById(R.id.trend_icon);
            sparkLine = view.findViewById(R.id.spark_line);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Coin coin, View view, int position);
    }
}
