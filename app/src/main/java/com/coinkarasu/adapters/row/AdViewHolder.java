package com.coinkarasu.adapters.row;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.coinkarasu.R;
import com.google.android.gms.ads.AdView;

public final class AdViewHolder extends RecyclerView.ViewHolder {
    public ViewGroup container;
    public AdView ad;

    public AdViewHolder(View view) {
        super(view);
        container = view.findViewById(R.id.ad_container);
        ad = null;
    }
}
