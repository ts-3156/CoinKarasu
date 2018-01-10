package com.coinkarasu.adapters.row;

import android.view.View;

import com.google.android.gms.ads.AdView;

public final class AdViewHolder extends CoinListViewHolder {
    public AdView ad;

    public AdViewHolder(View itemView) {
        super(itemView);
        ad = null;
    }
}
