package com.coinkarasu.adapters.row;

import android.view.View;

import com.coinkarasu.activities.TimeProvider;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.activities.etc.Section;
import com.coinkarasu.adapters.CoinListAdapter;
import com.coinkarasu.adapters.ResourceUtils;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.custom.AggressiveProgressbar;

import java.util.HashMap;
import java.util.Map;

public class HeaderDelegate extends UiManagingDelegate {
    public static final int TYPE = CoinListAdapter.TYPE_HEADER;

    private TimeProvider timeProvider;
    private ResourceUtils resources;
    private Map<Section, AggressiveProgressbar.Status> savedStatuses;

    public HeaderDelegate(TimeProvider timeProvider, ResourceUtils resources) {
        this.timeProvider = timeProvider;
        this.resources = resources;
        this.savedStatuses = new HashMap<>();
    }

    @Override
    public void onBindViewHolder(Coin coin, CoinListViewHolder _holder, OnCoinClickListener listener) {
        super.onBindViewHolder(coin, _holder, listener);

        HeaderViewHolder holder = (HeaderViewHolder) _holder;
        Section section = new Section(Exchange.valueOf(coin.getExchange()), coin.getCoinKind());

        holder.header.setText(resources.headerNameResIdStringMap.get(coin.getHeaderNameResId()));

        holder.progressbar.setTag(coin.getExchange() + "-" + coin.getCoinKind().name() + "-progressbar");
        holder.progressbar.setSection(section);
        if (savedStatuses.containsKey(section)) {
            holder.progressbar.setStatus(savedStatuses.get(section));
        }

        holder.timeSpan.setTag(coin.getExchange() + "-" + coin.getCoinKind().name() + "-time_span");
        holder.timeSpan.setSection(section);
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
        savedStatuses.put(holder.progressbar.getSection(), holder.progressbar.getStatus());
    }

    @Override
    public void onCoinClicked(Coin coin, View view, int position, NavigationKind kind) {
    }
}

