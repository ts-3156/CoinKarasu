package com.coinkarasu.activities;

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
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.activities.etc.TrendingKind;
import com.coinkarasu.adapters.HomeTabAdapter;
import com.coinkarasu.adapters.HomeTabHorizontalSpaceItemDecoration;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.custom.AggressiveProgressbar;
import com.coinkarasu.services.data.Trending;
import com.coinkarasu.utils.CKDateUtils;
import com.coinkarasu.utils.CKLog;

import java.util.ArrayList;
import java.util.List;


public class HomeTabCardFragment extends Fragment implements
        View.OnClickListener, HomeTabAdapter.OnItemClickListener, PopupMenu.OnMenuItemClickListener {

    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "HomeTabCardFragment";

    private TrendingKind kind;
    private Trending trending;
    private AggressiveProgressbar progressbar;
    private View popupIcon;
    private View popupLabel;

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

        progressbar = view.findViewById(R.id.progressbar);
        popupIcon = view.findViewById(R.id.popup_menu);
        popupLabel = view.findViewById(R.id.filter);

        popupIcon.setOnClickListener(this);
        popupLabel.setOnClickListener(this);

        initializeRecyclerView(view, kind);

        return view;
    }

    private void initializeRecyclerView(View view, TrendingKind kind) {
        if (DEBUG) CKLog.d(TAG, "initializeRecyclerView() " + kind.name());

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.addItemDecoration(new HomeTabHorizontalSpaceItemDecoration(getActivity(), getResources().getDimensionPixelSize(R.dimen.home_tab_horizontal_gap)));

        List<Coin> coins = new ArrayList<>();
        trending = Trending.restoreFromCache(getActivity(), kind);
        if (trending != null) {
            coins = trending.getCoins();
        }

        if (coins.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.warn_text)).setText(getString(R.string.home_tab_not_found));
            view.findViewById(R.id.warn_container).setVisibility(View.VISIBLE);
        } else {
            HomeTabAdapter adapter = new HomeTabAdapter(getActivity(), coins);
            adapter.setOnItemClickListener(this);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onItemClick(Coin coin, View view, int position) {
        CoinActivity.start(view.getContext(), coin, NavigationKind.home);
    }

    @Override
    public void onClick(View view) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(R.menu.trending_card);
        popup.setOnMenuItemClickListener(this);
        MenuItem item = popup.getMenu().findItem(R.id.action_last_updated);
        String str = "";

        if (trending != null && trending.getUpdated() != null) {
            str = CKDateUtils.getRelativeTimeSpanString(trending.getUpdated().getTime()).toString();
        }

        item.setTitle(getString(R.string.action_last_updated, str));

        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_filter_japan) {
        }

        return true;
    }

    public void showProgressbar() {
        popupIcon.setVisibility(View.GONE);
        popupLabel.setVisibility(View.GONE);
        progressbar.setVisibility(View.VISIBLE);
        progressbar.startAnimation();
    }
}
