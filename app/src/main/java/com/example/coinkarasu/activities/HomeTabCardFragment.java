package com.example.coinkarasu.activities;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.coinkarasu.R;
import com.example.coinkarasu.activities.MainFragment.NavigationKind;
import com.example.coinkarasu.adapters.HorizontalSpaceItemDecoration;
import com.example.coinkarasu.adapters.RecyclerViewAdapter;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.cryptocompare.data.CoinList;

import java.util.ArrayList;


public class HomeTabCardFragment extends Fragment implements OnItemClickListener {

    private static final String STATE_SELECTED_KIND_KEY = "kind";

    private CoinList coinList;
    private NavigationKind kind;
    private TabLayout.Tab tab;

    public HomeTabCardFragment() {
    }

    public static HomeTabCardFragment newInstance(NavigationKind kind) {
        HomeTabCardFragment fragment = new HomeTabCardFragment();
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
        View view = inflater.inflate(R.layout.fragment_home_tab_card, container, false);

        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf");
        ((TextView) view.findViewById(R.id.caption_left)).setTypeface(typeFace);
        ((TextView) view.findViewById(R.id.caption_right)).setTypeface(typeFace);
        ((TextView) view.findViewById(R.id.caption_desc_left)).setTypeface(typeFace);

        typeFace = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-LightItalic.ttf");
        ((TextView) view.findViewById(R.id.caption_desc_right)).setTypeface(typeFace);

        ((TextView) view.findViewById(R.id.caption_left)).setText(getString(R.string.caption_left, "TEST1", "TEST2"));

        ArrayList<Coin> coins = new ArrayList<>();
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.addItemDecoration(new HorizontalSpaceItemDecoration(getActivity(), 16));

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(getActivity(), coins);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        coinList = null;

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

    @Override
    public void onItemClick(Coin coin, View view, int position) {

    }
}
