package com.coinkarasu.adapters.row;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.adapters.CoinListAdapter;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.utils.CKLog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class AdDelegate extends UiManagingDelegate {
    private static final boolean DEBUG = true;
    private static final String TAG = "AdDelegate";
    public static final int TYPE = CoinListAdapter.TYPE_AD;

    public AdDelegate(Context context) {
    }

    @Override
    public void onBindViewHolder(Coin coin, CoinListViewHolder _holder, OnCoinClickListener listener) {
        super.onBindViewHolder(coin, _holder, listener);

        final AdViewHolder holder = (AdViewHolder) _holder;

        holder.ad = new AdView(holder.itemView.getContext());
        holder.ad.setAdSize(AdSize.SMART_BANNER);
        holder.ad.setAdUnitId(BuildConfig.ADMOB_UNIT_ID);

        holder.ad.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (holder.ad != null && holder.ad.getParent() == null) {
                    holder.container.setVisibility(View.VISIBLE);
                    holder.container.addView(holder.ad);
                    if (DEBUG) CKLog.d(TAG, "onAdLoaded()");
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                if (DEBUG) CKLog.e(TAG, "onAdFailedToLoad() " + errorCode);
            }
        });
        holder.ad.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void onViewRecycled(CoinListViewHolder _holder) {
        AdViewHolder holder = (AdViewHolder) _holder;

        if (holder.ad != null && holder.ad.getParent() != null) {
            ((ViewGroup) holder.ad.getParent()).removeView(holder.ad);
        } else {
            holder.container.removeAllViews();
        }
        holder.container.setVisibility(View.GONE);
        holder.ad = null;
    }

    @Override
    public void onCoinClicked(Coin coin, View view, int position, NavigationKind kind) {
    }
}

