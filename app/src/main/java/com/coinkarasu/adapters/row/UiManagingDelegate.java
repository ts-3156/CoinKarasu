package com.coinkarasu.adapters.row;

import android.view.View;

import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.coins.Coin;

public abstract class UiManagingDelegate {
    void onBindViewHolder(Coin coin, final CoinListViewHolder holder, final OnCoinClickListener listener, boolean isVisible) {
        if (holder.container != null) {
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onCoinClicked(view, holder);
                }
            });
        }
    }

    abstract void onViewRecycled(CoinListViewHolder holder);

    abstract void onCoinClicked(Coin coin, View view, int position, NavigationKind kind);
}
