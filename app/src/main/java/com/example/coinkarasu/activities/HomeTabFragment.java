package com.example.coinkarasu.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.coinkarasu.R;
import com.example.coinkarasu.cryptocompare.data.CoinList;
import com.example.coinkarasu.pagers.MainPagerAdapter;


public class HomeTabFragment extends Fragment implements MainPagerAdapter.Listener {

    public enum Kind {
        one_hour(R.string.caption_desc_1_hour, "frag_1_hour"),
        six_hours(R.string.caption_desc_6_hours, "frag_6_hours"),
        twelve_hours(R.string.caption_desc_12_hours, "frag_12_hours"),
        twenty_four_hours(R.string.caption_desc_24_hours, "frag_24_hours"),
        three_days(R.string.caption_desc_3_days, "frag_3_days");

        int labelResId;
        String tag;

        Kind(int labelResId, String tag) {
            this.labelResId = labelResId;
            this.tag = tag;
        }
    }

    private static final String STATE_SELECTED_KIND_KEY = "kind";

    private CoinList coinList;
    private NavigationKind kind;
    private TabLayout.Tab tab;

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

        getChildFragmentManager().beginTransaction()
                .replace(R.id.trending_1_hour, HomeTabCardFragment.newInstance(Kind.one_hour), Kind.one_hour.tag)
                .replace(R.id.trending_6_hours, HomeTabCardFragment.newInstance(Kind.six_hours), Kind.six_hours.tag)
                .replace(R.id.trending_12_hours, HomeTabCardFragment.newInstance(Kind.twelve_hours), Kind.twelve_hours.tag)
                .replace(R.id.trending_24_hours, HomeTabCardFragment.newInstance(Kind.twenty_four_hours), Kind.twenty_four_hours.tag)
                .replace(R.id.trending_3_days, HomeTabCardFragment.newInstance(Kind.three_days), Kind.three_days.tag)
                .commit();

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

        for (Kind k : Kind.values()) {
            Fragment fragment = manager.findFragmentByTag(k.tag);
            if (fragment != null) {
                transaction.remove(fragment);
            }
        }

        transaction.commitNowAllowingStateLoss();
    }
}
