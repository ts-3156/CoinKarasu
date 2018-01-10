package com.coinkarasu.adapters.row;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.coinkarasu.R;

public abstract class CoinListViewHolder extends RecyclerView.ViewHolder {
    public ViewGroup container;

    public CoinListViewHolder(View itemView) {
        super(itemView);
        container = itemView.findViewById(R.id.container);
    }

}
