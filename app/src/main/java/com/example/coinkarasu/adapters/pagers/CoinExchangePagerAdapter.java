package com.example.coinkarasu.adapters.pagers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.coinkarasu.activities.CoinExchangeTabContentFragment;
import com.example.coinkarasu.coins.SnapshotCoin;

import java.util.ArrayList;

public class CoinExchangePagerAdapter extends FragmentPagerAdapter {
    ArrayList<Fragment> fragments;

    public CoinExchangePagerAdapter(FragmentManager manager) {
        super(manager);
    }

    public CoinExchangePagerAdapter(FragmentManager manager, ArrayList<SnapshotCoin> coins) {
        super(manager);
        fragments = new ArrayList<>();

//        if (!coins.isEmpty()) {
//            ArrayList<String> exchanges = new ArrayList<>();
//            for (SnapshotCoin coin : coins) {
//                exchanges.add(coin.getMarket().toLowerCase());
//                if (exchanges.size() >= 3) {
//                    break;
//                }
//            }
//            String[] exchangesArray = exchanges.toArray(new String[exchanges.size()]);
//            fragments.add(CoinExchangesTabContentFragment.newInstance(
//                    coins.get(0).getFromSymbol(), coins.get(0).getToSymbol(), 0, exchangesArray));
//        }

        int offset = fragments.size();
        for (int i = 0; i < coins.size(); i++) {
            SnapshotCoin coin = coins.get(i);
            fragments.add(CoinExchangeTabContentFragment.newInstance(coin, i + offset, coin.getMarket().toLowerCase()));
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