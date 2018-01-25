package com.coinkarasu.pagers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.coinkarasu.activities.CoinPieChartFragment;
import com.coinkarasu.activities.CoinPieChartTabContentFragment;
import com.coinkarasu.api.cryptocompare.data.TopPair;

import java.util.ArrayList;
import java.util.List;

public class CoinPieChartPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;

    public CoinPieChartPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    public CoinPieChartPagerAdapter(FragmentManager manager, String fromSymbol, String toSymbol, List<TopPair> pairs) {
        super(manager);
        fragments = new ArrayList<>();

        fragments.add(CoinPieChartTabContentFragment.newInstance(CoinPieChartFragment.Kind.currency, fromSymbol, toSymbol));
        for (TopPair pair : pairs) {
            fragments.add(CoinPieChartTabContentFragment.newInstance(CoinPieChartFragment.Kind.exchange, fromSymbol, pair.getToSymbol()));
        }
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}