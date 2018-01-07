package com.coinkarasu.adapters.row;

import android.view.View;

import com.coinkarasu.coins.Coin;

public interface UiManagingDelegate {
    void onBindViewHolder(Coin coin, CoinListViewHolder holder);

    void onViewRecycled(CoinListViewHolder holder);

    void onCoinClicked(Coin coin, View view, int position);
}
