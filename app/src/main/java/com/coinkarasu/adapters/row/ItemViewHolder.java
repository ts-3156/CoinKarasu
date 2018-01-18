package com.coinkarasu.adapters.row;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.coinkarasu.R;
import com.coinkarasu.animator.PriceAnimator;
import com.coinkarasu.animator.PriceBgColorAnimator;
import com.coinkarasu.animator.PriceDiffAnimator;
import com.coinkarasu.animator.TrendAnimator;
import com.coinkarasu.custom.NetworkSparkView;

public final class ItemViewHolder extends CoinListViewHolder {
    public View innerContainer;
    public NetworkImageView icon;
    public TextView name;
    public TextView symbol;
    public TextView price;
    public TextView price_diff;
    public TextView trend;
    public ImageView trendIcon;
    public NetworkSparkView sparkLine;

    public PriceAnimator priceAnimator = null;
    public PriceDiffAnimator priceDiffAnimator = null;
    public PriceBgColorAnimator priceBgColorAnimator = null;
    public TrendAnimator trendAnimator = null;

    public ItemViewHolder(View itemView) {
        super(itemView);
        innerContainer = itemView.findViewById(R.id.inner_container);
        icon = itemView.findViewById(R.id.icon);
        name = itemView.findViewById(R.id.name);
        symbol = itemView.findViewById(R.id.symbol);
        price = itemView.findViewById(R.id.price);
        price_diff = itemView.findViewById(R.id.price_diff);
        trend = itemView.findViewById(R.id.trend);
        trendIcon = itemView.findViewById(R.id.trend_icon);
        sparkLine = itemView.findViewById(R.id.spark_line);
    }
}
