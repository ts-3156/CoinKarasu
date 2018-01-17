package com.coinkarasu.adapters.row;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coinkarasu.R;
import com.coinkarasu.activities.TimeProvider;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.adapters.CoinListAdapter;
import com.coinkarasu.adapters.ConfigUtils;
import com.coinkarasu.adapters.ResourceUtils;
import com.coinkarasu.coins.Coin;

public class UiManager implements OnCoinClickListener {
    private final RowDataProvider rowDataProvider;
    private final UiDelegatesFactory delegatesFactory;
    private NavigationKind kind;

    public UiManager(Context context, RowDataProvider rowDataProvider, TimeProvider timeProvider,
                     ResourceUtils resources, ConfigUtils configs, NavigationKind kind) {
        this.rowDataProvider = rowDataProvider;
        delegatesFactory = new UiDelegatesFactory(context, timeProvider, resources, configs);
        this.kind = kind;
    }

    public final CoinListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == CoinListAdapter.TYPE_HEADER) {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.coin_list_header_item, parent, false);
            return new HeaderViewHolder(item);
        } else if (viewType == CoinListAdapter.TYPE_AD) {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.coin_list_ad_item, parent, false);
            return new AdViewHolder(item);
        } else {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.coin_list_row_item, parent, false);
            return new ItemViewHolder(item);
        }
    }

    public void onBindViewHolder(Coin coin, CoinListViewHolder holder, boolean isVisible) {
        delegatesFactory.onBindViewHolder(coin, holder, this, isVisible);
    }

    public void onViewRecycled(CoinListViewHolder holder) {
        delegatesFactory.onViewRecycled(holder);
    }

    public void onCoinClicked(View view, CoinListViewHolder holder) {
        Coin coin = rowDataProvider.getItem(holder.getAdapterPosition());
        if (coin != null) {
            delegatesFactory.onCoinClicked(coin, view, holder, kind);
        }
    }
}
