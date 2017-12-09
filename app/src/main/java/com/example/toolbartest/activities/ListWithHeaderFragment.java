package com.example.toolbartest.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.toolbartest.R;
import com.example.toolbartest.adapters.CustomAdapter;
import com.example.toolbartest.coins.Coin;

import java.util.ArrayList;


public class ListWithHeaderFragment extends Fragment
        implements AdapterView.OnItemClickListener, ListView.OnScrollListener {

    private OnFragmentInteractionListener listener;

    String exchange;
    boolean textSeparatorVisibility;
    boolean dividerVisibility;

    public ListWithHeaderFragment() {
    }

    public static ListWithHeaderFragment newInstance(String exchange) {
        return newInstance(exchange, true, true);
    }

    public static ListWithHeaderFragment newInstance(String exchange, boolean textSeparatorVisibility, boolean dividerVisibility) {
        ListWithHeaderFragment fragment = new ListWithHeaderFragment();
        Bundle args = new Bundle();
        args.putString("exchange", exchange);
        args.putBoolean("textSeparatorVisibility", textSeparatorVisibility);
        args.putBoolean("dividerVisibility", dividerVisibility);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exchange = getArguments().getString("exchange");
            textSeparatorVisibility = getArguments().getBoolean("textSeparatorVisibility");
            dividerVisibility = getArguments().getBoolean("dividerVisibility");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_with_header, container, false);
        ArrayList<Coin> coins = ((MainActivity) getActivity()).filterCoins(exchange);

        TextView textSeparator = view.findViewById(R.id.text_separator);
        if (textSeparatorVisibility) {
            textSeparator.setText(exchangeToDisplayName());
            textSeparator.setVisibility(View.VISIBLE);
        } else {
            textSeparator.setVisibility(View.GONE);
        }

        View divider = view.findViewById(R.id.divider);
        if (dividerVisibility) {
            divider.setVisibility(View.VISIBLE);
        } else {
            divider.setVisibility(View.GONE);
        }

        CustomAdapter adapter = new CustomAdapter(getActivity(), coins);
        ListView listView = view.findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);

        return view;
    }

    public void updateView() {
        if (isDetached() || getView() == null) {
            return;
        }

        ArrayList<Coin> coins = ((MainActivity) getActivity()).filterCoins(exchange);

        ListView listView = getView().findViewById(R.id.list_view);
        if (listView != null) {
            CustomAdapter adapter = (CustomAdapter) listView.getAdapter();
            if (adapter != null) {
                adapter.replaceItems(coins);
            }
        }
    }

    public void setProgressbarVisibility(boolean enabled) {
        if (isDetached() || getView() == null) {
            return;
        }

        View view = getView().findViewById(R.id.progressbar);
        if (enabled && textSeparatorVisibility) {
            if (view.getVisibility() != View.VISIBLE) {
                view.setVisibility(View.VISIBLE);
            }
        } else {
            if (view.getVisibility() != View.GONE) {
                view.setVisibility(View.GONE);
            }
        }
    }

    private String exchangeToDisplayName() {
        String name;

        switch (exchange) {
            case "bitflyer":
                name = "BitFlyer";
                break;
            case "coincheck":
                name = "Coincheck";
                break;
            case "zaif":
                name = "Zaif";
                break;
            case "cccagg":
                name = "Aggregated Index";
                break;
            default:
                throw new RuntimeException("Invalid exchange " + exchange);
        }

        return name;
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
                ((CustomAdapter) listView.getAdapter()).setShowAnim(true);
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                ((CustomAdapter) listView.getAdapter()).setShowAnim(false);
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
