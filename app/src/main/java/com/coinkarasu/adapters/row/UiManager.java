package com.coinkarasu.adapters.row;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coinkarasu.R;
import com.coinkarasu.adapters.CoinListAdapter;
import com.coinkarasu.adapters.ConfigUtils;
import com.coinkarasu.adapters.ResourceUtils;
import com.coinkarasu.coins.Coin;

public class UiManager implements CoinListViewHolder.OnCoinClickListener {
    private final RowDataProvider rowDataProvider;
    private final UiDelegatesFactory delegatesFactory;

    public UiManager(Context context, RowDataProvider rowDataProvider, ResourceUtils resources, ConfigUtils configs) {
        this.rowDataProvider = rowDataProvider;
        delegatesFactory = new UiDelegatesFactory(context, resources, configs);
    }

    public final CoinListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == CoinListAdapter.TYPE_HEADER) {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.coin_list_header_item, parent, false);
            return new HeaderViewHolder(item, this);
        } else if (viewType == CoinListAdapter.TYPE_AD) {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.coin_list_ad_item, parent, false);
            return new AdViewHolder(item, this);
        } else {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.coin_list_row_item, parent, false);
            return new ItemViewHolder(item, this);
        }
    }

    public void onBindViewHolder(Coin coin, CoinListViewHolder holder) {
        delegatesFactory.onBindViewHolder(coin, holder);
    }

    public void onViewRecycled(CoinListViewHolder holder) {
        delegatesFactory.onViewRecycled(holder);
    }

    public void onCoinClicked(View view, CoinListViewHolder holder) {
        Coin coin = rowDataProvider.getItem(holder.getAdapterPosition());
        if (coin != null) {
            delegatesFactory.onCoinClicked(coin, view, holder);
        }
    }
}
