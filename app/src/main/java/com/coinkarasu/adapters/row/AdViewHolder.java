package com.coinkarasu.adapters.row;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.coinkarasu.R;
import com.google.android.gms.ads.AdView;

public final class AdViewHolder extends RecyclerView.ViewHolder {
    public AdView ad;

    public AdViewHolder(View view) {
        super(view);
        ad = view.findViewById(R.id.ad_view);
    }
}
