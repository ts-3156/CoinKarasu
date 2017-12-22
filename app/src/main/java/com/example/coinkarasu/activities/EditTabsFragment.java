package com.example.coinkarasu.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.coinkarasu.R;
import com.example.coinkarasu.adapters.EditTabsItemDecoration;
import com.example.coinkarasu.adapters.EditTabsRecyclerViewAdapter;
import com.example.coinkarasu.pagers.MainPagerAdapter;
import com.example.coinkarasu.utils.PrefHelper;


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
        recyclerView.addItemDecoration(new EditTabsItemDecoration(getActivity(), 16));

        EditTabsRecyclerViewAdapter adapter = new EditTabsRecyclerViewAdapter(getActivity());
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onItemClick(EditTabsRecyclerViewAdapter.Item item, View view, int position) {
        boolean isVisible = PrefHelper.isVisibleTab(getActivity(), item.kind);
        PrefHelper.setTabVisibility(getActivity(), item.kind, !isVisible);

        RecyclerView recyclerView = getView().findViewById(R.id.recycler_view);
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        adapter.notifyItemChanged(position);

        ((MainFragment) getParentFragment()).updateTabVisibility();
    }

    @Override
    public void removeAllNestedFragments() {
    }
}
