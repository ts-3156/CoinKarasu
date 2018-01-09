package com.coinkarasu.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.adapters.row.CoinListViewHolder;
import com.coinkarasu.adapters.row.RowDataProvider;
import com.coinkarasu.adapters.row.UiManager;
import com.coinkarasu.coins.Coin;

import java.util.ArrayList;
import java.util.List;

public class CoinListAdapter extends RecyclerView.Adapter<CoinListViewHolder> implements RowDataProvider {
    private static final boolean DEBUG = true;
    private static final String TAG = "CoinListAdapter";
    public static final int TYPE_ITEM = 0;
    public static final int TYPE_HEADER = 1;
    public static final int TYPE_AD = 2;

    private ArrayList<Coin> coins = new ArrayList<>();
    private UiManager uiManager;
    private ResourceUtils resources;
    private ConfigUtils configs;

    public CoinListAdapter(Context context, List<Coin> coins) {
        resources = new ResourceUtils(context, coins);
        configs = new ConfigUtils(context);
        uiManager = new UiManager(context, this, resources, configs);

        setHasStableIds(true);

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

    public void pauseAnimation() {
        configs.isAnimPaused = true;
    }

    public boolean isAnimPaused() {
        return configs.isAnimPaused;
    }

    public void resumeAnimation() {
        configs.isAnimPaused = false;
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

    @Override
    public void onBindViewHolder(CoinListViewHolder holder, int position) {
        uiManager.onBindViewHolder(getItem(position), holder);
    }

    @Override
    public void onViewRecycled(CoinListViewHolder holder) {
        uiManager.onViewRecycled(holder);
    }
}
