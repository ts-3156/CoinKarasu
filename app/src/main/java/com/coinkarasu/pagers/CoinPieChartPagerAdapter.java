package com.coinkarasu.pagers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.coinkarasu.activities.CoinPieChartFragment;
import com.coinkarasu.activities.CoinPieChartTabContentFragment;
import com.coinkarasu.coins.TopPairCoin;

import java.util.ArrayList;
import java.util.List;

public class CoinPieChartPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;

    public CoinPieChartPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    public CoinPieChartPagerAdapter(FragmentManager manager, String fromSymbol, String toSymbol, List<TopPairCoin> coins) {
        super(manager);
        fragments = new ArrayList<>();

        fragments.add(CoinPieChartTabContentFragment.newInstance(CoinPieChartFragment.Kind.currency, fromSymbol, toSymbol));
        for (TopPairCoin coin : coins) {
            fragments.add(CoinPieChartTabContentFragment.newInstance(CoinPieChartFragment.Kind.exchange, fromSymbol, coin.getToSymbol()));
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