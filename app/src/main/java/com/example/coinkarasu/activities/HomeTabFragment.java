package com.example.coinkarasu.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.coinkarasu.R;
import com.example.coinkarasu.activities.MainFragment.NavigationKind;
import com.example.coinkarasu.cryptocompare.data.CoinList;


public class HomeTabFragment extends Fragment {

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
                .replace(R.id.trending_1_hour, HomeTabCardFragment.newInstance(kind), "TAG")
                .replace(R.id.trending_6_hours, HomeTabCardFragment.newInstance(kind), "TAG")
                .replace(R.id.trending_12_hours, HomeTabCardFragment.newInstance(kind), "TAG")
                .replace(R.id.trending_24_hours, HomeTabCardFragment.newInstance(kind), "TAG")
                .replace(R.id.trending_3_days, HomeTabCardFragment.newInstance(kind), "TAG")
                .commit();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
}
