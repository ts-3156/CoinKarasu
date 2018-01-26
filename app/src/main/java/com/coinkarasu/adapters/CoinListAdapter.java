package com.coinkarasu.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.activities.etc.Section;
import com.coinkarasu.adapters.row.CoinListViewHolder;
import com.coinkarasu.adapters.row.RowDataProvider;
import com.coinkarasu.adapters.row.UiManager;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.custom.RelativeTimeSpanTextView;
import com.coinkarasu.utils.CKLog;

import java.util.ArrayList;
import java.util.List;

public class CoinListAdapter extends RecyclerView.Adapter<CoinListViewHolder> implements RowDataProvider {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "CoinListAdapter";
    public static final int TYPE_ITEM = 0;
    public static final int TYPE_HEADER = 1;
    public static final int TYPE_AD = 2;

    private List<Coin> coins;
    private UiManager uiManager;
    private ResourceUtils resources;
    private ConfigUtils configs;
    private RecyclerView.LayoutManager layoutManager;

    public CoinListAdapter(Context context, RelativeTimeSpanTextView.TimeProvider timeProvider, NavigationKind kind, List<Coin> coins) {
        resources = new ResourceUtils(context, coins);
        configs = new ConfigUtils(context);
        uiManager = new UiManager(context, this, timeProvider, resources, configs, kind);

        setHasStableIds(true);

        this.coins = new ArrayList<>();
        for (Coin coin : coins) {
            addItem(coin);
        }
        notifyDataSetChanged();
    }

    public void setIsScrolled(boolean flag) {
        configs.isScrolled = flag;
    }

    public void setAnimEnabled(boolean flag) {
        configs.isAnimEnabled = flag;
    }

    public void setToSymbol(String symbol) {
        for (Coin coin : coins) {
            coin.setToSymbol(symbol);
        }
        resources.toSymbolChanged(symbol);
    }

    public void setDownloadIconEnabled(boolean flag) {
        configs.isDownloadIconEnabled = flag;
    }

    public void startAnimation(Section section) {
        configs.startAnimation(section);
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

    @Override
    public Coin getItem(int position) {
        return coins.get(position);
    }

    public List<Coin> getItems(Exchange exchange, CoinKind coinKind) {
        List<Coin> filtered = new ArrayList<>();
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

    @Override
    public CoinListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return uiManager.onCreateViewHolder(parent, viewType);
    }

    /**
     * スクロールにより画面から消えるとonDetachedFromWindow
     * スクロールにより画面に表示されるとonAttachedToWindow、onVisibilityChanged(true)
     * 画面に表示されていない時に定期的(？)にonAttachedToWindow、onVisibilityChanged(true)、onDetachedFromWindow
     * 画面に表示されていない時にデータが更新されると、onViewRecycled、onBindViewHolder、onAttachedToWindow、onVisibilityChanged(true)、onDetachedFromWindow
     * 画面に表示されている時にデータが更新されると、onBindViewHolder
     */
    @Override
    public void onBindViewHolder(CoinListViewHolder holder, int position) {
        boolean isVisible = true; // できるだけ、見えているアイテムのみアニメーションさせるフラグ
        if (layoutManager instanceof LinearLayoutManager) {
            int last = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            isVisible = last == -1 || position <= last + 1;
        }
        uiManager.onBindViewHolder(getItem(position), holder, isVisible);
    }

    @Override
    public void onViewRecycled(CoinListViewHolder holder) {
        uiManager.onViewRecycled(holder);
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }
}
