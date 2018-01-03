package com.coinkarasu.activities;

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

import com.coinkarasu.R;
import com.coinkarasu.activities.etc.TrendingKind;
import com.coinkarasu.adapters.HomeTabHorizontalSpaceItemDecoration;
import com.coinkarasu.adapters.HomeTabRecyclerViewAdapter;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.data.Trending;

import java.util.ArrayList;


public class HomeTabCardFragment extends Fragment implements
        View.OnClickListener, HomeTabRecyclerViewAdapter.OnItemClickListener, PopupMenu.OnMenuItemClickListener {

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

        ((TextView) view.findViewById(R.id.caption_desc)).setText(getString(kind.labelResId));

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
        recyclerView.addItemDecoration(new HomeTabHorizontalSpaceItemDecoration(getActivity(), (int) getResources().getDimension(R.dimen.home_tab_horizontal_gap)));

        ArrayList<Coin> coins = new ArrayList<>();
        Trending trending = Trending.restoreFromCache(getActivity(), kind);
        if (trending != null) {
            coins = trending.getCoins();
        }

        if (coins.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            ((TextView) getView().findViewById(R.id.warn_text)).setText(getString(R.string.home_tab_not_found));
            getView().findViewById(R.id.warn_container).setVisibility(View.VISIBLE);
        } else {
            HomeTabRecyclerViewAdapter adapter = new HomeTabRecyclerViewAdapter(getActivity(), coins);
            adapter.setOnItemClickListener(this);
            recyclerView.setAdapter(adapter);
        }
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
        intent.putExtra(CoinActivity.KEY_COIN_JSON, coin.toJson().toString());
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.inflate(R.menu.trending_card);
        popup.setOnMenuItemClickListener(this);

        MenuItem item = popup.getMenu().findItem(R.id.action_filter);
        item.setChecked(isFilterChecked);
        item.setEnabled(false);

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
