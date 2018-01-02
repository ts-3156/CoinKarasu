package com.coinkarasu.pagers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.coinkarasu.activities.CoinLineChartFragment;
import com.coinkarasu.activities.CoinLineChartTabContentFragment;

public class CoinLineChartPagerAdapter extends FragmentPagerAdapter {
    String fromSymbol;
    String toSymbol;

    public CoinLineChartPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    public CoinLineChartPagerAdapter(FragmentManager manager, String fromSymbol, String toSymbol) {
        super(manager);
        this.fromSymbol = fromSymbol;
        this.toSymbol = toSymbol;
    }

    @Override
    public Fragment getItem(int position) {
        return CoinLineChartTabContentFragment.newInstance(CoinLineChartFragment.Kind.values()[position], fromSymbol, toSymbol);
    }

    @Override
    public int getCount() {
        return CoinLineChartFragment.Kind.values().length;
    }
}