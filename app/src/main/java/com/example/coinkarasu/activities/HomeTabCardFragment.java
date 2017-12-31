package com.example.coinkarasu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.coinkarasu.R;
import com.example.coinkarasu.activities.etc.TrendingKind;
import com.example.coinkarasu.adapters.HomeTabHorizontalSpaceItemDecoration;
import com.example.coinkarasu.adapters.HomeTabRecyclerViewAdapter;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.data.Trending;

import java.util.ArrayList;


public class HomeTabCardFragment extends Fragment implements
        View.OnClickListener, OnItemClickListener, PopupMenu.OnMenuItemClickListener {

    private static final String STATE_SELECTED_KIND_KEY = "kind";

    private TrendingKind kind;
    private boolean isFilterChecked;

    public HomeTabCardFragment() {
    }

    public static HomeTabCardFragment newInstance(TrendingKind kind) {
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
            kind = TrendingKind.valueOf(getArguments().getString("kind"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_tab_card, container, false);

        ((TextView) view.findViewById(R.id.caption_desc_right)).setText(getString(kind.labelResId));

        view.findViewById(R.id.popup_menu).setOnClickListener(this);
        view.findViewById(R.id.filter).setOnClickListener(this);

        isFilterChecked = true;

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeRecyclerView();
    }

    private void initializeRecyclerView() {
        if (getView() == null) {
            return;
        }

        RecyclerView recyclerView = getView().findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.addItemDecoration(new HomeTabHorizontalSpaceItemDecoration(getActivity(), 16));

        ArrayList<Coin> coins = new ArrayList<>();
        Trending trending = Trending.restoreFromCache(getActivity(), kind);
        if (trending != null) {
            coins = trending.getCoins();
        }
        HomeTabRecyclerViewAdapter adapter = new HomeTabRecyclerViewAdapter(getActivity(), coins);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
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
        Intent intent = new Intent(view.getContext(), CoinActivity.class);
        intent.putExtra(CoinActivity.COIN_NAME_KEY, coin.toJson().toString());
        intent.putExtra(CoinActivity.COIN_SYMBOL_KEY, coin.getSymbol());
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.inflate(R.menu.trending_card);
        popup.setOnMenuItemClickListener(this);

        MenuItem item = popup.getMenu().findItem(R.id.action_filter);
        item.setChecked(isFilterChecked);

        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            isFilterChecked = !item.isChecked();
            item.setChecked(isFilterChecked);
            if (item.isChecked()) {

            } else {
            }
        }

        return true;
    }
}
