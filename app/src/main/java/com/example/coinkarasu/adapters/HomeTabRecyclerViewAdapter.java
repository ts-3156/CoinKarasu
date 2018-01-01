package com.example.coinkarasu.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.example.coinkarasu.R;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.format.PriceFormat;
import com.example.coinkarasu.format.TrendValueFormat;

import java.util.ArrayList;

public class HomeTabRecyclerViewAdapter extends RecyclerView.Adapter<HomeTabRecyclerViewAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private OnItemClickListener listener;
    private ArrayList<Coin> coins;

    private ResourceUtils resources;

    public HomeTabRecyclerViewAdapter(Context context, ArrayList<Coin> coins) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.coins = coins;

        resources = new ResourceUtils(context, coins);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.recycler_home_tab_row_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Coin coin = coins.get(position);

        holder.symbol.setText(coin.getSymbol());
        holder.price.setText(new PriceFormat(coin.getToSymbol()).format(coin.getPrice()));
        holder.trend.setText(new TrendValueFormat().format(coin.getTrend()));
        holder.trend.setTextColor(resources.getTrendColor(coin.getTrend()));

        holder.icon.setDefaultImageResId(resources.symbolIconResIdMap.get(coin.getSymbol()));
        holder.icon.setImageUrl(coin.getLargeImageUrl(), resources.imageLoader);

        holder.trendIcon.setImageResource(resources.trendIconFormat.format(coin.getTrend()));

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(coin, view, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return coins.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        View container;
        NetworkImageView icon;
        TextView symbol;
        TextView price;
        TextView trend;
        ImageView trendIcon;

        ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            icon = view.findViewById(R.id.icon);
            symbol = view.findViewById(R.id.symbol);
            price = view.findViewById(R.id.price);
            trend = view.findViewById(R.id.trend);
            trendIcon = view.findViewById(R.id.trend_icon);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Coin coin, View view, int position);
    }
}
