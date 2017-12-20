package com.example.coinkarasu.activities;

import android.content.Context;
import android.graphics.Typeface;
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
import com.example.coinkarasu.adapters.HorizontalSpaceItemDecoration;
import com.example.coinkarasu.adapters.RecyclerViewAdapter;
import com.example.coinkarasu.coins.Coin;

import java.util.ArrayList;

import static com.example.coinkarasu.activities.HomeTabFragment.Kind;


public class HomeTabCardFragment extends Fragment implements
        View.OnClickListener, OnItemClickListener, PopupMenu.OnMenuItemClickListener {

    private static final String STATE_SELECTED_KIND_KEY = "kind";

    private Kind kind;
    private boolean isFilterChecked;

    public HomeTabCardFragment() {
    }

    public static HomeTabCardFragment newInstance(Kind kind) {
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
            kind = Kind.valueOf(getArguments().getString("kind"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_tab_card, container, false);

        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf");
//        ((TextView) view.findViewById(R.id.caption_left)).setTypeface(typeFace);
        ((TextView) view.findViewById(R.id.caption_right)).setTypeface(typeFace);
        ((TextView) view.findViewById(R.id.caption_desc_left)).setTypeface(typeFace);

        typeFace = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-LightItalic.ttf");
        ((TextView) view.findViewById(R.id.caption_desc_right)).setTypeface(typeFace);

//        ((TextView) view.findViewById(R.id.caption_left)).setText(getString(R.string.caption_left, "TEST1", "TEST2"));

        ((TextView) view.findViewById(R.id.caption_desc_right)).setText(getString(kind.labelResId));

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.addItemDecoration(new HorizontalSpaceItemDecoration(getActivity(), 16));

        ArrayList<Coin> coins = new ArrayList<>();
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(getActivity(), coins);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.popup_menu).setOnClickListener(this);
        view.findViewById(R.id.filter).setOnClickListener(this);

        isFilterChecked = true;

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
