package com.coinkarasu.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.coinkarasu.R;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.activities.etc.TrendingKind;
import com.coinkarasu.adapters.HomeTabAdapter;
import com.coinkarasu.adapters.HomeTabHorizontalSpaceItemDecoration;
import com.coinkarasu.adapters.HomeTabVerticalSpaceItemDecoration;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.custom.AggressiveProgressbar;
import com.coinkarasu.services.data.Trending;
import com.coinkarasu.utils.CKDateUtils;
import com.coinkarasu.utils.CKLog;


public class HomeTabCardFragment extends Fragment implements
        View.OnClickListener,
        HomeTabAdapter.OnItemClickListener,
        PopupMenu.OnMenuItemClickListener {

    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "HomeTabCardFragment";

    private TrendingKind kind;
    private Trending trending;
    private AggressiveProgressbar progressbar;
    private View popupIcon;
    private View popupLabel;
    private HomeTabAdapter adapter;
    private SwitchCompat filterSwitch;
    private RecyclerView recyclerView;
    private TextView warning;
    private View warningContainer;
    private PopupMenu popup;

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

        ((TextView) view.findViewById(R.id.card_title)).setText(kind.titleResId);
        ((TextView) view.findViewById(R.id.card_duration)).setText(kind.durationResId);

        filterSwitch = view.findViewById(R.id.card_filter_only_trending);
        filterSwitch.setChecked(kind.filterOnlyTrending);
        filterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (adapter != null) {
                    adapter.setFilterOnlyTrending(isChecked);
                    setEmptyCoinsWarning(adapter.getItemCount() == 0);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        warning = view.findViewById(R.id.warn_text);
        warning.setText(getString(R.string.home_tab_empty_coins));
        warningContainer = view.findViewById(R.id.warn_container);

        progressbar = view.findViewById(R.id.progressbar);
        popupIcon = view.findViewById(R.id.popup_menu);
        popupLabel = view.findViewById(R.id.filter);

        popupIcon.setOnClickListener(this);
        popupLabel.setOnClickListener(this);

        recyclerView = view.findViewById(R.id.recycler_view);
        initializeRecyclerView(kind);

        return view;
    }

    private void initializeRecyclerView(final TrendingKind kind) {
        if (DEBUG) CKLog.d(TAG, "initializeRecyclerView() " + kind.name());

        int gap = getResources().getDimensionPixelSize(R.dimen.home_tab_horizontal_gap);

        if (kind.shouldUseGridLayout) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (recyclerView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
                    params.setMarginStart(gap);
                    params.setMarginEnd(gap);
                }
            }

            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int spanCount = (screenWidth - 2 * gap) / getResources().getDimensionPixelSize(R.dimen.home_tab_max_width);
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
            recyclerView.addItemDecoration(new HomeTabVerticalSpaceItemDecoration(gap, spanCount));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
            recyclerView.addItemDecoration(new HomeTabHorizontalSpaceItemDecoration(gap));
        }

        new RestoreTrendingTask().setListener(new RestoreTrendingTask.Listener() {
            @Override
            public void restored(Trending result) {
                trending = result;

                if (trending == null || trending.getCoins() == null || trending.getCoins().isEmpty()) {
                    // この時点でcoinsが空の場合は、アダプターすら作成しない
                    setEmptyCoinsWarning(true);
                } else {
                    adapter = new HomeTabAdapter(getActivity(), trending.getCoins(), filterSwitch.isChecked());
                    adapter.setTrendingKind(kind);
                    adapter.setOnItemClickListener(HomeTabCardFragment.this);
                    recyclerView.setAdapter(adapter);
                }
            }
        }).execute(getActivity(), kind);
    }

    public void setEmptyCoinsWarning(boolean set) {
        recyclerView.setVisibility(set ? View.GONE : View.VISIBLE);
        warningContainer.setVisibility(set ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(Coin coin, View view, int position) {
        CoinActivity.start(view.getContext(), coin, NavigationKind.home);
    }

    @Override
    public void onClick(View view) {
        if (popup == null) {
            popup = new PopupMenu(view.getContext(), view);
            popup.inflate(R.menu.trending_card);
            popup.setOnMenuItemClickListener(this);

            MenuItem item = popup.getMenu().findItem(R.id.action_use_grid_layout);
            item.setChecked(kind.shouldUseGridLayout);
        }
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
        if (item.getItemId() == R.id.action_use_grid_layout) {
        }

        return true;
    }

    public void showProgressbar() {
        popupIcon.setVisibility(View.GONE);
        popupLabel.setVisibility(View.GONE);
        progressbar.setVisibility(View.VISIBLE);
        progressbar.startAnimation();
    }

    private static class RestoreTrendingTask extends AsyncTask<Object, Void, Trending> {
        private Listener listener;

        @Override
        protected Trending doInBackground(Object... params) {
            return Trending.restoreFromCache((Context) params[0], (TrendingKind) params[1]);
        }

        @Override
        protected void onPostExecute(Trending result) {
            if (listener != null) {
                listener.restored(result);
                listener = null;
            }
        }

        RestoreTrendingTask setListener(Listener listener) {
            this.listener = listener;
            return this;
        }

        public interface Listener {
            void restored(Trending trending);
        }
    }
}
