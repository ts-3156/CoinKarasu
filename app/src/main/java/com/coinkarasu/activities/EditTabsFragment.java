package com.coinkarasu.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coinkarasu.R;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.adapters.EditTabsItemDecoration;
import com.coinkarasu.adapters.EditTabsRecyclerViewAdapter;
import com.coinkarasu.pagers.MainPagerAdapter;
import com.coinkarasu.utils.PrefHelper;


public class EditTabsFragment extends Fragment implements
        EditTabsRecyclerViewAdapter.OnItemClickListener,
        MainPagerAdapter.Listener {

    public EditTabsFragment() {
    }

    public static EditTabsFragment newInstance() {
        return new EditTabsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_tabs, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new EditTabsItemDecoration(getActivity(), getResources().getDimensionPixelSize(R.dimen.edit_tabs_vertical_gap)));

        EditTabsRecyclerViewAdapter adapter = new EditTabsRecyclerViewAdapter(getActivity());
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onItemClick(EditTabsRecyclerViewAdapter.Item item, View view, int position) {
        if (item.kind == NavigationKind.bitflyer || item.kind == NavigationKind.zaif) {
            Snackbar.make(getView(), getString(R.string.edit_tabs_is_not_available, getString(item.kind.tabStrResId)), Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }

        if (!(item.kind.isHideable() && item.kind.isShowable())) {
            return;
        }

        boolean isAdded = PrefHelper.toggleTabVisibility(getActivity(), item.kind);

        RecyclerView recyclerView = getView().findViewById(R.id.recycler_view);
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        adapter.notifyItemChanged(position);

        Snackbar.make(getView(), isAddedOrRemoved(item.kind, isAdded), Snackbar.LENGTH_SHORT)
                .show();

        ((MainFragment) getParentFragment()).updateTabVisibility();
    }

    private String isAddedOrRemoved(NavigationKind kind, boolean isAdded) {
        String name = getString(kind.tabStrResId);
        String str;

        if (isAdded) {
            str = getString(R.string.edit_tabs_is_added, name);
        } else {
            str = getString(R.string.edit_tabs_is_removed, name);
        }

        return str;
    }

    @Override
    public void removeAllNestedFragments() {
    }
}
