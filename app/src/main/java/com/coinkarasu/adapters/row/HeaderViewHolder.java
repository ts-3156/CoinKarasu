package com.coinkarasu.adapters.row;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.coinkarasu.R;
import com.coinkarasu.custom.RelativeTimeSpanTextView;

public final class HeaderViewHolder extends RecyclerView.ViewHolder {
    public View container;
    public TextView header;
    public View divider;
    public View progressbar;
    public RelativeTimeSpanTextView timeSpan;

    public HeaderViewHolder(View view) {
        super(view);
        container = view.findViewById(R.id.container);
        header = view.findViewById(R.id.text_separator);
        divider = view.findViewById(R.id.divider);
        progressbar = view.findViewById(R.id.progressbar);
        timeSpan = view.findViewById(R.id.relative_time_span);
    }
}
