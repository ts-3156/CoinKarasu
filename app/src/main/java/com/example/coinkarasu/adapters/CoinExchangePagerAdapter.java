package com.example.coinkarasu.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.coinkarasu.activities.CoinExchangeTabContentFragment;
import com.example.coinkarasu.activities.CoinPieChartFragment;
import com.example.coinkarasu.activities.CoinPieChartTabContentFragment;
import com.example.coinkarasu.coins.SnapshotCoin;
import com.example.coinkarasu.cryptocompare.data.TopPair;

import java.util.ArrayList;

public class CoinExchangePagerAdapter extends FragmentPagerAdapter {
    ArrayList<Fragment> fragments;

    public CoinExchangePagerAdapter(FragmentManager manager) {
        super(manager);
    }

    public CoinExchangePagerAdapter(FragmentManager manager, String fromSymbol, String toSymbol, ArrayList<SnapshotCoin> coins) {
        super(manager);
        fragments = new ArrayList<>();

        for (int i = 0; i < coins.size(); i++) {
            SnapshotCoin coin = coins.get(i);
            fragments.add(CoinExchangeTabContentFragment.newInstance(coin, fromSymbol, toSymbol, i, coin.getMarket().toLowerCase()));
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