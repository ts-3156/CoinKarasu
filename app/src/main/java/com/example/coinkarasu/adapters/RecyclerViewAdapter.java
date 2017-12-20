package com.example.coinkarasu.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.example.coinkarasu.R;
import com.example.coinkarasu.activities.OnItemClickListener;
import com.example.coinkarasu.coins.Coin;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private OnItemClickListener listener;
    private ArrayList<Coin> coins;

    public RecyclerViewAdapter(Context context, ArrayList<Coin> coins) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.coins = coins;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.recycler_row_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.symbol.setText("SYM");
        holder.price.setText("1,000,000");
        holder.trend.setText("10%");
        holder.icon.setDefaultImageResId(R.drawable.ic_btc_test_192);

        holder.symbol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(coins.get(position), view, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 10;
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
