package com.example.coinkarasu.pagers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.coinkarasu.activities.CoinPieChartFragment;
import com.example.coinkarasu.activities.CoinPieChartTabContentFragment;
import com.example.coinkarasu.cryptocompare.data.TopPair;

import java.util.ArrayList;

public class CoinPieChartPagerAdapter extends FragmentPagerAdapter {
    ArrayList<Fragment> fragments;

    public CoinPieChartPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    public CoinPieChartPagerAdapter(FragmentManager manager, String fromSymbol, String toSymbol, ArrayList<TopPair> pairs) {
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