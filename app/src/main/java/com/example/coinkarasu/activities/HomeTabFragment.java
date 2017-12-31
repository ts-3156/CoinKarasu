package com.example.coinkarasu.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.coinkarasu.R;
import com.example.coinkarasu.activities.etc.NavigationKind;
import com.example.coinkarasu.activities.etc.TrendingKind;
import com.example.coinkarasu.pagers.MainPagerAdapter;


public class HomeTabFragment extends Fragment implements MainPagerAdapter.Listener {

    private static final String STATE_SELECTED_KIND_KEY = "kind";

    private NavigationKind kind;

    public HomeTabFragment() {
    }

    public static HomeTabFragment newInstance(NavigationKind kind) {
        HomeTabFragment fragment = new HomeTabFragment();
        Bundle args = new Bundle();
        args.putString("kind", kind.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            kind = NavigationKind.valueOf(getArguments().getString("kind"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_tab, container, false);

        if (savedInstanceState != null) {
        } else {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.trending_1_hour, HomeTabCardFragment.newInstance(TrendingKind.one_hour), TrendingKind.one_hour.tag)
                    .replace(R.id.trending_6_hours, HomeTabCardFragment.newInstance(TrendingKind.six_hours), TrendingKind.six_hours.tag)
                    .replace(R.id.trending_12_hours, HomeTabCardFragment.newInstance(TrendingKind.twelve_hours), TrendingKind.twelve_hours.tag)
                    .replace(R.id.trending_24_hours, HomeTabCardFragment.newInstance(TrendingKind.twenty_four_hours), TrendingKind.twenty_four_hours.tag)
                    .replace(R.id.trending_3_days, HomeTabCardFragment.newInstance(TrendingKind.three_days), TrendingKind.three_days.tag)
                    .commit();
        }


        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        kind = null;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_SELECTED_KIND_KEY, kind.name());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void removeAllNestedFragments() {
        if (!isAdded() || isDetached()) {
            return;
        }

        FragmentManager manager = getChildFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        for (TrendingKind k : TrendingKind.values()) {
            Fragment fragment = manager.findFragmentByTag(k.tag);
            if (fragment != null) {
                transaction.remove(fragment);
            }
        }

        transaction.commitNowAllowingStateLoss();
    }
}
