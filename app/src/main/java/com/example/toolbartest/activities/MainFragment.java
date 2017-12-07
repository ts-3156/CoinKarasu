package com.example.toolbartest.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.toolbartest.R;
import com.example.toolbartest.adapters.CustomAdapter;
import com.example.toolbartest.coins.Coin;

import java.util.ArrayList;


public class MainFragment extends Fragment {

    private OnFragmentInteractionListener listener;

    public MainFragment() {
    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        CustomAdapter adapter = new CustomAdapter(getActivity(), ((MainActivity) getActivity()).getSectionInsertedCoins());
        ListView listView = view.findViewById(R.id.coin_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                Coin coin = (Coin) ((ListView) parent).getItemAtPosition(pos);
                if (coin.isSectionHeader()) {
                    return;
                }

                ((MainActivity) getActivity()).stopAutoUpdateTimer();
                Intent intent = new Intent(view.getContext(), CoinActivity.class);
                intent.putExtra(MainActivity.COIN_ACTIVITY_COIN_NAME_KEY, coin.getCoinName());
                intent.putExtra(MainActivity.COIN_ACTIVITY_COIN_SYMBOL_KEY, coin.getSymbol());
                startActivity(intent);
            }
        });

        return view;
    }

    public void updateCoinListView(ArrayList<Coin> coins) {
        if (isDetached() || getView() == null) {
            return;
        }

        ListView listView = getView().findViewById(R.id.coin_list);

        if (listView != null) {
            CustomAdapter adapter = (CustomAdapter) listView.getAdapter();
            if (adapter != null) {
                adapter.replaceItems(coins);
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
