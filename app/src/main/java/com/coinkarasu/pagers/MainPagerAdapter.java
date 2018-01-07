package com.coinkarasu.pagers;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.coinkarasu.activities.CoinListFragment;
import com.coinkarasu.activities.EditTabsFragment;
import com.coinkarasu.activities.HomeTabFragment;
import com.coinkarasu.activities.etc.NavigationKind;

import java.util.ArrayList;

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

        switch (selectedKind) {
            case home:
                fragment = HomeTabFragment.newInstance(selectedKind);
                break;
//            case assets:
//                fragment = HomeTabFragment.newInstance(selectedKind);
//                break;
            case edit_tabs:
                fragment = EditTabsFragment.newInstance();
                break;
            default:
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
}