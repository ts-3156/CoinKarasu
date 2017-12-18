package com.example.coinkarasu.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.coinkarasu.activities.ListViewFragment;
import com.example.coinkarasu.activities.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class MainPagerAdapter extends FragmentPagerAdapter {
    MainActivity.NavigationKind defaultKind;

    public MainPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    public MainPagerAdapter(FragmentManager manager, MainActivity.NavigationKind defaultKind) {
        super(manager);
        this.defaultKind = defaultKind;
    }

    @Override
    public Fragment getItem(int position) {
        MainActivity.NavigationKind selectedKind = MainActivity.NavigationKind.values()[position];
        return ListViewFragment.newInstance(selectedKind, defaultKind == selectedKind);
    }

    @Override
    public int getCount() {
        return MainActivity.NavigationKind.values().length;
    }
}