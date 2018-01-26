package com.coinkarasu.pagers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.coinkarasu.activities.etc.HistoricalPriceKind;
import com.coinkarasu.activities.CoinLineChartTabContentFragment;

public class HistoricalPricePagerAdapter extends FragmentPagerAdapter {
    private String fromSymbol;
    private String toSymbol;

    public HistoricalPricePagerAdapter(FragmentManager manager) {
        super(manager);
    }

    public HistoricalPricePagerAdapter(FragmentManager manager, String fromSymbol, String toSymbol) {
        super(manager);
        this.fromSymbol = fromSymbol;
        this.toSymbol = toSymbol;
    }

    @Override
    public Fragment getItem(int position) {
        return CoinLineChartTabContentFragment.newInstance(HistoricalPriceKind.values()[position], fromSymbol, toSymbol);
    }

    @Override
    public int getCount() {
        return HistoricalPriceKind.values().length;
    }
}
