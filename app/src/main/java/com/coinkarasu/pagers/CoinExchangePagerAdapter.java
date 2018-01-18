package com.coinkarasu.pagers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.coinkarasu.activities.CoinExchangeTabContentFragment;
import com.coinkarasu.coins.SnapshotCoin;

import java.util.ArrayList;
import java.util.List;

public class CoinExchangePagerAdapter extends FragmentPagerAdapter {
    List<Fragment> fragments;

    public CoinExchangePagerAdapter(FragmentManager manager) {
        super(manager);
    }

    public CoinExchangePagerAdapter(FragmentManager manager, List<SnapshotCoin> coins) {
        super(manager);
        fragments = new ArrayList<>();

        for (int i = 0; i < coins.size(); i++) {
            SnapshotCoin coin = coins.get(i);
            fragments.add(CoinExchangeTabContentFragment.newInstance(coin, i, coin.getMarket().toLowerCase()));
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