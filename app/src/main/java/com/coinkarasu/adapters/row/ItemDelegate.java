package com.coinkarasu.adapters.row;

import android.view.View;

import com.coinkarasu.activities.CoinActivity;
import com.coinkarasu.adapters.CoinListAdapter;
import com.coinkarasu.adapters.ConfigUtils;
import com.coinkarasu.adapters.ResourceUtils;
import com.coinkarasu.animator.PriceAnimator;
import com.coinkarasu.animator.PriceBgColorAnimator;
import com.coinkarasu.animator.PriceDiffAnimator;
import com.coinkarasu.animator.TrendAnimator;
import com.coinkarasu.coins.Coin;

public class ItemDelegate implements UiManagingDelegate {
    public static final int TYPE = CoinListAdapter.TYPE_ITEM;

    private ResourceUtils resources;
    private ConfigUtils configs;

    public ItemDelegate(ResourceUtils resources, ConfigUtils configs) {
        this.resources = resources;
        this.configs = configs;
    }

    @Override
    public void onBindViewHolder(Coin coin, CoinListViewHolder _holder) {
        ItemViewHolder holder = (ItemViewHolder) _holder;

        holder.icon.setDefaultImageResId(resources.symbolIconResIdMap.get(coin.getSymbol()));
        if (configs.isDownloadIconEnabled) {
            holder.icon.setImageUrl(coin.getFullImageUrl(), resources.imageLoader);
        }

        holder.name.setText(coin.getCoinName());
        holder.symbol.setText(coin.getSymbol());
        holder.price_diff.setTextColor(resources.getPriceColor(coin.getPriceDiff()));
        holder.trend.setTextColor(resources.getTrendColor(coin.getTrend()));
        holder.trendIcon.setImageResource(resources.trendIconFormat.format(coin.getTrend()));

        if (!configs.isAnimPaused && configs.isAnimEnabled && !configs.isScrolled) {
            holder.priceAnimator = new PriceAnimator(coin, holder.price);
            holder.priceAnimator.start();

            if (coin.getPrice() > coin.getPrevPrice()) {
                holder.priceBgColorAnimator = new PriceBgColorAnimator(resources.priceUpFromColor, resources.priceToColor, holder.innerContainer);
            } else if (coin.getPrice() < coin.getPrevPrice()) {
                holder.priceBgColorAnimator = new PriceBgColorAnimator(resources.priceDownFromColor, resources.priceToColor, holder.innerContainer);
            }
            if (holder.priceBgColorAnimator != null) {
                holder.priceBgColorAnimator.start();
            }

            holder.priceDiffAnimator = new PriceDiffAnimator(coin, holder.price_diff);
            holder.priceDiffAnimator.start();

            holder.trendAnimator = new TrendAnimator(coin, holder.trend);
            holder.trendAnimator.start();
        } else {
            holder.price.setText(resources.priceFormatter.format(coin.getPrice()));
            holder.price_diff.setText(resources.signedPriceFormatter.format(coin.getPriceDiff()));
            holder.trend.setText(resources.surroundedTrendFormatter.format(coin.getTrend()));
            holder.innerContainer.setBackgroundColor(resources.priceToColor);

        }
    }

    @Override
    public void onViewRecycled(CoinListViewHolder _holder) {
        ItemViewHolder holder = (ItemViewHolder) _holder;

        holder.icon.setImageUrl(null, resources.imageLoader);

        if (holder.priceAnimator != null) {
            holder.priceAnimator.cancel();
            holder.priceAnimator = null;
        }
        if (holder.priceDiffAnimator != null) {
            holder.priceDiffAnimator.cancel();
            holder.priceDiffAnimator = null;
        }
        if (holder.priceBgColorAnimator != null) {
            holder.priceBgColorAnimator.cancel();
            holder.priceBgColorAnimator = null;
        }
        if (holder.trendAnimator != null) {
            holder.trendAnimator.cancel();
            holder.trendAnimator = null;
        }

        holder.container.setOnClickListener(null);
    }

    @Override
    public void onCoinClicked(Coin coin, View view, int position) {
        CoinActivity.start(view.getContext(), coin);
    }
}

