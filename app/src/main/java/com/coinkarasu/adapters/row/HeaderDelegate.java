package com.coinkarasu.adapters.row;

import android.view.View;

import com.coinkarasu.activities.TimeProvider;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.adapters.CoinListAdapter;
import com.coinkarasu.adapters.ResourceUtils;
import com.coinkarasu.coins.Coin;

public class HeaderDelegate implements UiManagingDelegate {
    public static final int TYPE = CoinListAdapter.TYPE_HEADER;

    private TimeProvider timeProvider;
    private ResourceUtils resources;

    public HeaderDelegate(TimeProvider timeProvider, ResourceUtils resources) {
        this.timeProvider = timeProvider;
        this.resources = resources;
    }

    @Override
    public void onBindViewHolder(Coin coin, CoinListViewHolder _holder) {
        HeaderViewHolder holder = (HeaderViewHolder) _holder;

        holder.header.setText(resources.headerNameResIdStringMap.get(coin.getHeaderNameResId()));
        holder.progressbar.setTag(coin.getExchange() + "-" + coin.getCoinKind().name() + "-progressbar");
        holder.timeSpan.setTag(coin.getExchange() + "-" + coin.getCoinKind().name() + "-time_span");
        holder.timeSpan.setTimeProvider(timeProvider);

        if (holder.getAdapterPosition() == 0) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewRecycled(CoinListViewHolder _holder) {
        HeaderViewHolder holder = (HeaderViewHolder) _holder;
        holder.container.setOnClickListener(null);
    }

    @Override
    public void onCoinClicked(Coin coin, View view, int position, NavigationKind kind) {
    }
}

