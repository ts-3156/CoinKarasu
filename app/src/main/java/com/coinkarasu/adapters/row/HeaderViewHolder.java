package com.coinkarasu.adapters.row;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.coinkarasu.R;

public final class HeaderViewHolder extends RecyclerView.ViewHolder {
    public View container;
    public TextView header;
    public View divider;
    public View progressbar;
    public View timeSpanContainer;

    public HeaderViewHolder(View view) {
        super(view);
        container = view.findViewById(R.id.container);
        header = view.findViewById(R.id.text_separator);
        divider = view.findViewById(R.id.divider);
        progressbar = view.findViewById(R.id.progressbar);
        timeSpanContainer = view.findViewById(R.id.time_span_container);
    }
}
