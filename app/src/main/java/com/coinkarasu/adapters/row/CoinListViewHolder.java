package com.coinkarasu.adapters.row;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.coinkarasu.R;

public abstract class CoinListViewHolder extends RecyclerView.ViewHolder {
    public ViewGroup container;

    public CoinListViewHolder(View itemView, final OnCoinClickListener listener) {
        super(itemView);
        container = itemView.findViewById(R.id.container);
        if (container != null) {
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onCoinClicked(view, CoinListViewHolder.this);
                }
            });
        }
    }

    public interface OnCoinClickListener {
        void onCoinClicked(View view, CoinListViewHolder holder);
    }
}
