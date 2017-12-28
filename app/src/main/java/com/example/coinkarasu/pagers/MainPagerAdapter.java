package com.example.coinkarasu.pagers;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.example.coinkarasu.activities.CoinListFragment;
import com.example.coinkarasu.activities.EditTabsFragment;
import com.example.coinkarasu.activities.HomeTabFragment;
import com.example.coinkarasu.activities.ListViewByExchangeFragment;

import java.util.ArrayList;

import com.example.coinkarasu.activities.etc.NavigationKind;

public class MainPagerAdapter extends FragmentPagerAdapter {
    private static final boolean DEBUG = false;

    private Context context;
    private NavigationKind defaultKind;
    private ArrayList<Fragment> fragments;
    private long version;

    public MainPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    public MainPagerAdapter(FragmentManager manager, Context context, NavigationKind defaultKind) {
        super(manager);
        this.context = context;
        this.defaultKind = defaultKind;
        this.fragments = new ArrayList<>();
        this.version = System.currentTimeMillis();
    }

    @Override
    public Fragment getItem(int position) {
        NavigationKind selectedKind = NavigationKind.visibleValues(context).get(position);
        Fragment fragment;

        if (DEBUG) Log.e("getItem", "" + selectedKind);

        if (selectedKind == NavigationKind.home) {
            fragment = HomeTabFragment.newInstance(selectedKind);
        } else if (selectedKind == NavigationKind.edit_tabs) {
            fragment = EditTabsFragment.newInstance();
        } else if (selectedKind == NavigationKind.coincheck) {
            fragment = ListViewByExchangeFragment.newInstance(selectedKind, defaultKind == selectedKind);
        } else {
            fragment = CoinListFragment.newInstance(selectedKind, defaultKind == selectedKind);
        }

        fragments.add(fragment);

        return fragment;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void notifyDataSetChanged() {
        for (Fragment fragment : fragments) {
            ((Listener) fragment).removeAllNestedFragments();
        }
        fragments.clear();
        version = System.currentTimeMillis();
        super.notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position + version;
    }

    @Override
    public int getCount() {
        return NavigationKind.visibleValues(context).size();
    }

    public interface Listener {
        void removeAllNestedFragments();
    }
}