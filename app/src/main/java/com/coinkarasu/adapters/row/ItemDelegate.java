package com.coinkarasu.adapters.row;

import android.view.View;

import com.coinkarasu.activities.CoinActivity;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.activities.etc.Section;
import com.coinkarasu.adapters.CoinListAdapter;
import com.coinkarasu.adapters.Configurations;
import com.coinkarasu.adapters.ResourceUtils;
import com.coinkarasu.animator.PriceAnimator;
import com.coinkarasu.animator.PriceBgColorAnimator;
import com.coinkarasu.animator.PriceDiffAnimator;
import com.coinkarasu.animator.TrendAnimator;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.utils.CKLog;

public class ItemDelegate extends UiManagingDelegate {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "ItemDelegate";
    public static final int TYPE = CoinListAdapter.TYPE_ITEM;

    private ResourceUtils resources;
    private Configurations configs;

    public ItemDelegate(ResourceUtils resources, Configurations configs) {
        this.resources = resources;
        this.configs = configs;
    }

    @Override
    public void onBindViewHolder(Coin coin, CoinListViewHolder _holder, OnCoinClickListener listener, boolean isVisible) {
        super.onBindViewHolder(coin, _holder, listener, isVisible);

        ItemViewHolder holder = (ItemViewHolder) _holder;

        holder.sparkLine.setConfigurations(configs);
        holder.sparkLine.setSymbols(coin.getSymbol(), coin.getToSymbol());

        holder.icon.setDefaultImageResId(resources.symbolIconResIdMap.get(coin.getSymbol()));
        if (configs.isDownloadIconEnabled) {
            holder.icon.setImageUrl(coin.getFullImageUrl(), resources.imageLoader);
        }

        holder.name.setText(coin.getCoinName());
        holder.symbol.setText(coin.getSymbol());
        holder.priceDiff.setTextColor(resources.getPriceColor(coin.getPriceDiff()));
        holder.trend.setTextColor(resources.getTrendColor(coin.getTrend()));
        holder.trendIcon.setImageResource(resources.trendIconFormat.format(coin.getTrend()));

        Section section = new Section(Exchange.valueOf(coin.getExchange()), coin.getCoinKind());

        if (isVisible && configs.isAnimStarted(section) && configs.isAnimEnabled && !configs.isBeingScrolled) {
            holder.priceAnimator = new PriceAnimator(coin, holder.price);
            holder.priceAnimator.start();

            int fromColor = coin.getPrice() > coin.getPrevPrice() ? resources.priceUpFromColor : resources.priceDownFromColor;
            holder.priceBgColorAnimator = new PriceBgColorAnimator(fromColor, resources.priceToColor, holder.innerContainer);
            holder.priceBgColorAnimator.start();

            holder.priceDiffAnimator = new PriceDiffAnimator(coin, holder.priceDiff);
            holder.priceDiffAnimator.start();

            holder.trendAnimator = new TrendAnimator(coin, holder.trend);
            holder.trendAnimator.start();
        } else {
            holder.price.setText(resources.getPriceFormatter(coin.getToSymbol()).format(coin.getPrice()));
            holder.priceDiff.setText(resources.signedPriceFormatter.format(coin.getPriceDiff()));
            holder.trend.setText(resources.surroundedTrendFormatter.format(coin.getTrend()));
            holder.innerContainer.setBackgroundColor(resources.priceToColor);
        }
    }

    @Override
    public void onViewRecycled(CoinListViewHolder _holder) {
        ItemViewHolder holder = (ItemViewHolder) _holder;

        holder.sparkLine.clearData();
        holder.icon.setImageUrl(null, null);

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
    public void onCoinClicked(Coin coin, View view, int position, NavigationKind kind) {
        CoinActivity.start(view.getContext(), coin, kind);
    }
}

