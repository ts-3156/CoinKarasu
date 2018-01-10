package com.coinkarasu.adapters.row;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;

import com.coinkarasu.activities.TimeProvider;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.adapters.ConfigUtils;
import com.coinkarasu.adapters.ResourceUtils;
import com.coinkarasu.coins.Coin;

public class UiDelegatesFactory {
    private SparseArray<UiManagingDelegate> uiDelegates;

    public UiDelegatesFactory(Context context, TimeProvider timeProvider, ResourceUtils resources, ConfigUtils configs) {
        uiDelegates = new SparseArray<>();
        uiDelegates.put(AdDelegate.TYPE, new AdDelegate(context));
        uiDelegates.put(HeaderDelegate.TYPE, new HeaderDelegate(timeProvider, resources));
        uiDelegates.put(ItemDelegate.TYPE, new ItemDelegate(resources, configs));
    }

    public void onBindViewHolder(Coin coin, CoinListViewHolder holder) {
        uiDelegates.get(holder.getItemViewType()).onBindViewHolder(coin, holder);
    }

    public void onViewRecycled(CoinListViewHolder holder) {
        uiDelegates.get(holder.getItemViewType()).onViewRecycled(holder);
    }

    public void onCoinClicked(Coin coin, View view, CoinListViewHolder holder, NavigationKind kind) {
        uiDelegates.get(holder.getItemViewType()).onCoinClicked(coin, view, holder.getAdapterPosition(), kind);
    }
}
