package com.coinkarasu.adapters.row;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.coinkarasu.R;
import com.coinkarasu.animator.PriceAnimator;
import com.coinkarasu.animator.PriceBgColorAnimator;
import com.coinkarasu.animator.PriceDiffAnimator;
import com.coinkarasu.animator.TrendAnimator;

public final class ItemViewHolder extends RecyclerView.ViewHolder {
    public View container;
    public NetworkImageView icon;
    public TextView name;
    public TextView symbol;
    public TextView price;
    public TextView price_diff;
    public TextView trend;
    public ImageView trendIcon;

    public PriceAnimator priceAnimator = null;
    public PriceDiffAnimator priceDiffAnimator = null;
    public PriceBgColorAnimator priceBgColorAnimator = null;
    public TrendAnimator trendAnimator = null;

    public ItemViewHolder(View view) {
        super(view);
        container = view.findViewById(R.id.inner_container);
        icon = view.findViewById(R.id.icon);
        name = view.findViewById(R.id.name);
        symbol = view.findViewById(R.id.symbol);
        price = view.findViewById(R.id.price);
        price_diff = view.findViewById(R.id.price_diff);
        trend = view.findViewById(R.id.trend);
        trendIcon = view.findViewById(R.id.trend_icon);
    }
}
