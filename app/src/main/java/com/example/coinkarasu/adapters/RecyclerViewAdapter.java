package com.example.coinkarasu.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.coinkarasu.R;
import com.example.coinkarasu.activities.OnItemClickListener;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.format.SignedPriceFormat;
import com.example.coinkarasu.format.TrendValueFormat;

import java.util.ArrayList;
import java.util.HashMap;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private OnItemClickListener listener;
    private ArrayList<Coin> coins;

    private ResourceUtils resources;

    public RecyclerViewAdapter(Context context, ArrayList<Coin> coins) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.coins = coins;

        resources = new ResourceUtils(context, coins);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.recycler_row_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Coin coin = coins.get(position);

        holder.symbol.setText(coin.getSymbol());
        holder.price.setText(new SignedPriceFormat(coin.getToSymbol()).format(coin.getPrice() - coin.getPrevPrice()));
        holder.trend.setText(new TrendValueFormat().format(coin.getTrend()));
        holder.trend.setTextColor(resources.getTrendColor(coin.getTrend()));

        holder.icon.setDefaultImageResId(resources.symbolIconResIdMap.get(coin.getSymbol()));
        holder.icon.setImageUrl(coin.getLargeImageUrl(), resources.imageLoader);

        holder.symbol.setOnClickListener(new View.OnClickListener() {
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

        NetworkImageView icon;
        TextView symbol;
        TextView price;
        TextView trend;

        ViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.icon);
            symbol = view.findViewById(R.id.symbol);
            price = view.findViewById(R.id.price);
            trend = view.findViewById(R.id.trend);
        }
    }
}
