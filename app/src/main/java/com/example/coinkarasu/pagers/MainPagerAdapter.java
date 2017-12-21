package com.example.coinkarasu.pagers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.coinkarasu.activities.EditTabsFragment;
import com.example.coinkarasu.activities.HomeTabFragment;
import com.example.coinkarasu.activities.ListViewFragment;

import static com.example.coinkarasu.activities.MainFragment.NavigationKind;

public class MainPagerAdapter extends FragmentPagerAdapter {
    NavigationKind defaultKind;

    public MainPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    public MainPagerAdapter(FragmentManager manager, NavigationKind defaultKind) {
        super(manager);
        this.defaultKind = defaultKind;
    }

    @Override
    public Fragment getItem(int position) {
        NavigationKind selectedKind = NavigationKind.values()[position];
        if (selectedKind == NavigationKind.home) {
            return HomeTabFragment.newInstance(selectedKind);
        } else if (selectedKind == NavigationKind.edit_tabs) {
            return EditTabsFragment.newInstance();
        } else {
            return ListViewFragment.newInstance(selectedKind, defaultKind == selectedKind);
        }
    }

    @Override
    public int getCount() {
        return NavigationKind.values().length;
    }
}