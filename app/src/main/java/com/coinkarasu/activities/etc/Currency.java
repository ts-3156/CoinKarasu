package com.coinkarasu.activities.etc;

import com.coinkarasu.R;

public enum Currency {
    JPY(R.string.action_currency_switch_to_usd, R.string.action_currency_only_for_jpy),
    USD(R.string.action_currency_switch_to_jpy, -1);

    public int titleStrResId;
    public int disabledTitleStrResId;

    Currency(int titleStrResId, int disabledTitleStrResId) {
        this.titleStrResId = titleStrResId;
        this.disabledTitleStrResId = disabledTitleStrResId;
    }
}
