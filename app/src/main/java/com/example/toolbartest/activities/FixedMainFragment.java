package com.example.toolbartest.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.toolbartest.R;
import com.example.toolbartest.adapters.CustomAdapter;
import com.example.toolbartest.coins.Coin;

import java.util.ArrayList;
import java.util.List;


public class FixedMainFragment extends Fragment {

    private OnFragmentInteractionListener listener;

    public FixedMainFragment() {
    }

    public static FixedMainFragment newInstance() {
        return new FixedMainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fixed_main, container, false);
        ArrayList<Coin> coins = ((MainActivity) getActivity()).getSectionInsertedCoins();

        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                Coin coin = (Coin) ((ListView) parent).getItemAtPosition(pos);
                if (coin.isSectionHeader()) {
                    return;
                }

                ((MainActivity) getActivity()).stopAutoUpdate();
                Intent intent = new Intent(view.getContext(), CoinActivity.class);
                intent.putExtra(MainActivity.COIN_ACTIVITY_COIN_NAME_KEY, coin.getCoinName());
                intent.putExtra(MainActivity.COIN_ACTIVITY_COIN_SYMBOL_KEY, coin.getSymbol());
                startActivity(intent);
            }
        };

        int[] resIds = {R.id.list_bitflyer, R.id.list_coincheck, R.id.list_zaif};
        List<List<Coin>> coinsCoins = new ArrayList<>();
        coinsCoins.add(coins.subList(1, 2));
        coinsCoins.add(coins.subList(3, 4));
        coinsCoins.add(coins.subList(5, coins.size()));

        for (int i = 0; i < resIds.length; i++) {
            int resId = resIds[i];
            List<Coin> targetCoins = coinsCoins.get(i);

            CustomAdapter adapter = new CustomAdapter(getActivity(), targetCoins);
            ListView listView = view.findViewById(resId);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(listener);
        }

        return view;
    }

    public void updateCoinListView(ArrayList<Coin> coins) {
        if (isDetached() || getView() == null) {
            return;
        }

        View view = getView();
        int[] resIds = {R.id.list_bitflyer, R.id.list_coincheck, R.id.list_zaif};
        List<List<Coin>> coinsCoins = new ArrayList<>();
        coinsCoins.add(coins.subList(1, 2));
        coinsCoins.add(coins.subList(3, 4));
        coinsCoins.add(coins.subList(5, coins.size()));

        for (int i = 0; i < resIds.length; i++) {
            int resId = resIds[i];
            List<Coin> targetCoins = coinsCoins.get(i);

            ListView listView = view.findViewById(resId);
            if (listView != null) {
                CustomAdapter adapter = (CustomAdapter) listView.getAdapter();
                if (adapter != null) {
                    adapter.replaceItems(targetCoins);
                }
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
