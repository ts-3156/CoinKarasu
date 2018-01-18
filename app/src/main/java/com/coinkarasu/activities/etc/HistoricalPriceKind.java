package com.coinkarasu.activities.etc;

import com.coinkarasu.R;

public enum HistoricalPriceKind {
    hour(R.string.line_chart_label_1_hour),
    day(R.string.line_chart_label_1_day),
    week(R.string.line_chart_label_1_week),
    month(R.string.line_chart_label_1_month),
    year(R.string.line_chart_label_1_year);

    public int labelResId;

    HistoricalPriceKind(int labelResId) {
        this.labelResId = labelResId;
    }
}
