package com.example.toolbartest.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.toolbartest.R;
import com.example.toolbartest.adapters.CustomAdapter;
import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.format.PriceViewFormat;
import com.example.toolbartest.utils.AnimHelper;

import java.util.ArrayList;


public class ListWithHeaderFragment extends Fragment
        implements AdapterView.OnItemClickListener, ListView.OnScrollListener {

    private OnFragmentInteractionListener listener;

    String[] exchanges;
    boolean textSeparatorVisibility;
    boolean dividerVisibility;

    public ListWithHeaderFragment() {
    }

    public static ListWithHeaderFragment newInstance(String exchange) {
        return newInstance(exchange, true, true);
    }

    public static ListWithHeaderFragment newInstance(String exchange, boolean textSeparatorVisibility, boolean dividerVisibility) {
        return newInstance(new String[]{exchange}, textSeparatorVisibility, dividerVisibility);
    }

    public static ListWithHeaderFragment newInstance(String[] exchanges) {
        return newInstance(exchanges, true, true);
    }

    public static ListWithHeaderFragment newInstance(String[] exchanges, boolean textSeparatorVisibility, boolean dividerVisibility) {
        ListWithHeaderFragment fragment = new ListWithHeaderFragment();
        Bundle args = new Bundle();
        args.putStringArray("exchanges", exchanges);
        args.putBoolean("textSeparatorVisibility", textSeparatorVisibility);
        args.putBoolean("dividerVisibility", dividerVisibility);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exchanges = getArguments().getStringArray("exchanges");
            textSeparatorVisibility = getArguments().getBoolean("textSeparatorVisibility");
            dividerVisibility = getArguments().getBoolean("dividerVisibility");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_with_header, container, false);
        ArrayList<Coin> coins = ((MainActivity) getActivity()).groupedCoins(exchanges);

        CustomAdapter adapter = new CustomAdapter(getActivity(), coins);
        ListView listView = view.findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);

        exchanges = null;

        return view;
    }

    public void updateView() {
        if (isDetached() || getView() == null) {
            return;
        }

        ListView listView = getView().findViewById(R.id.list_view);
        if (listView != null) {
            CustomAdapter adapter = (CustomAdapter) listView.getAdapter();
            if (adapter != null) {
//                adapter.replaceItems(coins);
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void setProgressbarVisibilityDelayed(final boolean flag, final String exchange) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setProgressbarVisibility(flag, exchange);
            }
        }, PriceViewFormat.DURATION);
    }

    public void setProgressbarVisibility(boolean flag, String exchange) {
        if (isDetached() || getView() == null) {
            return;
        }

        View view = getView().findViewWithTag(exchange);
        if (view == null) {
            return;
        }

        if (flag && textSeparatorVisibility) {
            if (view.getVisibility() != View.VISIBLE) {
                view.setVisibility(View.VISIBLE);
            }
        } else {
            if (view.getVisibility() != View.GONE) {
                view.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnFragmentInteractionListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        Coin coin = (Coin) ((ListView) parent).getItemAtPosition(pos);
        if (coin.isSectionHeader()) {
            return;
        }

        ((MainActivity) getActivity()).stopAutoUpdate();
        Intent intent = new Intent(view.getContext(), CoinActivity.class);
        intent.putExtra(CoinActivity.COIN_NAME_KEY, coin.toJson().toString());
        intent.putExtra(CoinActivity.COIN_SYMBOL_KEY, coin.getSymbol());
        startActivity(intent);
    }

    @Override
    public void onScrollStateChanged(AbsListView listView, int state) {
        switch (state) {
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                ((CustomAdapter) listView.getAdapter()).setAnimEnabled(true);
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                ((CustomAdapter) listView.getAdapter()).setAnimEnabled(false);
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                break;
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
