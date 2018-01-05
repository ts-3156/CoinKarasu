package com.coinkarasu.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.R;
import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.adapters.row.AdViewHolder;
import com.coinkarasu.adapters.row.HeaderViewHolder;
import com.coinkarasu.adapters.row.ItemViewHolder;
import com.coinkarasu.animator.PriceAnimator;
import com.coinkarasu.animator.PriceBgColorAnimator;
import com.coinkarasu.animator.PriceDiffAnimator;
import com.coinkarasu.animator.TrendAnimator;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.utils.Log;
import com.coinkarasu.utils.PrefHelper;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

public class CoinListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final boolean DEBUG = true;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_AD = 2;

    private OnItemClickListener listener;
    private LayoutInflater inflater;
    private ArrayList<Coin> coins = new ArrayList<>();
    private boolean isAnimEnabled;
    private boolean isDownloadIconEnabled;
    private boolean isScrolled;
    private boolean isAnimPaused;

    private ResourceUtils resources;
    private Log logger;

    public CoinListAdapter(Activity activity, List<Coin> coins) {
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        isAnimEnabled = PrefHelper.isAnimEnabled(activity);
        isDownloadIconEnabled = PrefHelper.isDownloadIconEnabled(activity);
        isScrolled = false;
        isAnimPaused = false;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        resources = new ResourceUtils(activity, coins);

        setHasStableIds(true);

        for (Coin coin : coins) {
            addItem(coin);
        }
        notifyDataSetChanged();

        logger = new Log(activity);
    }

    public void setIsScrolled(boolean flag) {
        this.isScrolled = flag;
    }

    public void setAnimEnabled(boolean flag) {
        this.isAnimEnabled = flag;
    }

    public void setToSymbol(String symbol) {
        for (Coin coin : coins) {
            coin.setToSymbol(symbol);
        }
        resources.toSymbolChanged(symbol);
    }

    public void setDownloadIconEnabled(boolean flag) {
        this.isDownloadIconEnabled = flag;
    }

    public void pauseAnimation() {
        isAnimPaused = true;
    }

    public void restartAnimation() {
        isAnimPaused = false;
    }

    public void addItem(Coin coin) {
        coins.add(coin);
    }

    @Override
    public int getItemViewType(int position) {
        Coin coin = coins.get(position);
        if (coin.isSectionHeader()) {
            return TYPE_HEADER;
        } else if (coin.isAdCoin()) {
            return TYPE_AD;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return coins.size();
    }


    public ArrayList<Coin> getItems(Exchange exchange, CoinKind coinKind) {
        ArrayList<Coin> filtered = new ArrayList<>();
        for (Coin coin : coins) {
            if (coin.isSectionHeader()
                    || coin.isAdCoin()
                    || !coin.getExchange().equals(exchange.name())
                    || coin.getCoinKind() != coinKind) {
                continue;
            }

            filtered.add(coin);
        }

        return filtered;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void notifyCoinsChanged(Exchange exchange, CoinKind coinKind) {
        for (int i = 0; i < coins.size(); i++) {
            Coin coin = coins.get(i);
            if (coin.isSectionHeader()
                    || coin.isAdCoin()
                    || !coin.getExchange().equals(exchange.name())
                    || coin.getCoinKind() != coinKind) {
                continue;
            }

            if (coin.isChanged()) {
                notifyItemChanged(i);
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return new HeaderViewHolder(inflater.inflate(R.layout.coin_list_header_item, parent, false));
            case TYPE_AD:
                return new AdViewHolder(inflater.inflate(R.layout.coin_list_ad_item, parent, false));
            case TYPE_ITEM:
                return new ItemViewHolder(inflater.inflate(R.layout.coin_list_row_item, parent, false));
            default:
                throw new RuntimeException("Invalid viewType " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_HEADER:
                bindHeaderViewHolder((HeaderViewHolder) holder, position);
                break;
            case TYPE_AD:
                bindAdViewHolder((AdViewHolder) holder, position);
                break;
            case TYPE_ITEM:
                bindItemViewHolder((ItemViewHolder) holder, position);
                break;
        }
    }

    private void bindHeaderViewHolder(final HeaderViewHolder holder, int position) {
        final Coin coin = coins.get(position);

        holder.header.setText(resources.headerNameResIdStringMap.get(coin.getHeaderNameResId()));
        holder.progressbar.setTag(coin.getExchange() + "-" + coin.getCoinKind().name() + "-progressbar");
        holder.timeSpan.setTag(coin.getExchange() + "-" + coin.getCoinKind().name() + "-time_span");

        if (position == 0) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(coin, view, holder.getAdapterPosition());
            }
        });
    }

    private void bindAdViewHolder(final AdViewHolder holder, int position) {
        holder.ad = new AdView(holder.itemView.getContext());
        holder.ad.setAdSize(AdSize.SMART_BANNER);
        holder.ad.setAdUnitId(BuildConfig.ADMOB_UNIT_ID);

        holder.ad.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (holder.ad != null && holder.ad.getParent() == null) {
                    holder.container.setVisibility(View.VISIBLE);
                    holder.container.addView(holder.ad);
                    if (DEBUG) logger.d("onAdLoaded", "loaded");
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                if (DEBUG) logger.e("bindAdViewHolder", "onAdFailedToLoad() " + errorCode);
            }
        });
        holder.ad.loadAd(new AdRequest.Builder().build());
    }

    private void bindItemViewHolder(final ItemViewHolder holder, int position) {
        final Coin coin = coins.get(position);

        holder.icon.setDefaultImageResId(resources.symbolIconResIdMap.get(coin.getSymbol()));
        if (isDownloadIconEnabled) {
            holder.icon.setImageUrl(coin.getFullImageUrl(), resources.imageLoader);
        }

        holder.name.setText(coin.getCoinName());
        holder.symbol.setText(coin.getSymbol());
        holder.price_diff.setTextColor(resources.getPriceColor(coin.getPriceDiff()));
        holder.trend.setTextColor(resources.getTrendColor(coin.getTrend()));
        holder.trendIcon.setImageResource(resources.trendIconFormat.format(coin.getTrend()));

        if (!isAnimPaused && isAnimEnabled && !isScrolled) {
            if (coin.getPrice() != coin.getPrevPrice()) {
                holder.priceAnimator = new PriceAnimator(coin, holder.price);
                holder.priceAnimator.start();

                if (coin.getPrice() > coin.getPrevPrice()) {
                    holder.priceBgColorAnimator = new PriceBgColorAnimator(resources.priceUpFromColor, resources.priceToColor, holder.container);
                } else {
                    holder.priceBgColorAnimator = new PriceBgColorAnimator(resources.priceDownFromColor, resources.priceToColor, holder.container);
                }
                holder.priceBgColorAnimator.start();
            } else {
                holder.price.setText(resources.priceFormatter.format(coin.getPrice()));
            }

            if (coin.getPriceDiff() != coin.getPrevPriceDiff()) {
                holder.priceDiffAnimator = new PriceDiffAnimator(coin, holder.price_diff);
                holder.priceDiffAnimator.start();
            } else {
                holder.price_diff.setText(resources.signedPriceFormatter.format(coin.getPriceDiff()));
            }

            if (coin.getTrend() != coin.getPrevTrend()) {
                holder.trendAnimator = new TrendAnimator(coin, holder.trend);
                holder.trendAnimator.start();
            } else {
                holder.trend.setText(resources.surroundedTrendFormatter.format(coin.getTrend()));
            }
        } else {
            holder.price.setText(resources.priceFormatter.format(coin.getPrice()));
            holder.price_diff.setText(resources.signedPriceFormatter.format(coin.getPriceDiff()));
            holder.trend.setText(resources.surroundedTrendFormatter.format(coin.getTrend()));
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(coin, view, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        switch (holder.getItemViewType()) {
            case TYPE_HEADER:
                headerViewRecycled((HeaderViewHolder) holder);
                break;
            case TYPE_AD:
                adViewRecycled((AdViewHolder) holder);
                break;
            case TYPE_ITEM:
                itemViewRecycled((ItemViewHolder) holder);
                break;
        }
    }

    private void headerViewRecycled(HeaderViewHolder holder) {
        holder.container.setOnClickListener(null);
    }

    private void adViewRecycled(AdViewHolder holder) {
        if (holder.ad != null && holder.ad.getParent() != null) {
            ((ViewGroup) holder.ad.getParent()).removeView(holder.ad);
        } else {
            holder.container.removeAllViews();
        }
        holder.container.setVisibility(View.GONE);
        holder.ad = null;
    }

    private void itemViewRecycled(ItemViewHolder holder) {
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

    public interface OnItemClickListener {
        void onItemClick(Coin coin, View view, int position);
    }
}
