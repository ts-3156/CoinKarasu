package com.coinkarasu.adapters.row;

import android.view.View;
import android.widget.TextView;

import com.coinkarasu.R;
import com.coinkarasu.custom.AggressiveProgressbar;
import com.coinkarasu.custom.RelativeTimeSpanTextView;

public final class HeaderViewHolder extends CoinListViewHolder {
    public TextView header;
    public View divider;
    public AggressiveProgressbar progressbar;
    public RelativeTimeSpanTextView timeSpan;

    public HeaderViewHolder(View itemView) {
        super(itemView);
        header = itemView.findViewById(R.id.text_separator);
        divider = itemView.findViewById(R.id.divider);
        progressbar = itemView.findViewById(R.id.progressbar);
        timeSpan = itemView.findViewById(R.id.relative_time_span);
    }
}
