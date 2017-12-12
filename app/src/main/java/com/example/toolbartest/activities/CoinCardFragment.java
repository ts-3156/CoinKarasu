package com.example.toolbartest.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.toolbartest.R;
import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.coins.CoinImpl;
import com.example.toolbartest.format.PriceViewFormat;
import com.example.toolbartest.format.TrendViewFormat;
import com.example.toolbartest.utils.IconHelper;

import org.json.JSONException;
import org.json.JSONObject;


public class CoinCardFragment extends Fragment {

    private OnFragmentInteractionListener listener;

    private String kind;
    private Coin coin;

    public CoinCardFragment() {
    }

    public static CoinCardFragment newInstance(String kind, String coinJson) {
        CoinCardFragment fragment = new CoinCardFragment();
        Bundle args = new Bundle();
        args.putString("kind", kind);
        args.putString("coinJson", coinJson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            kind = getArguments().getString("kind");
            String coinJson = getArguments().getString("coinJson");

            try {
                coin = CoinImpl.buildByJSONObject(new JSONObject(coinJson));
            } catch (JSONException e) {
                Log.d("onCreate", e.getMessage());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_card, container, false);

        new PriceViewFormat(coin).format((TextView) view.findViewById(R.id.price));
        new TrendViewFormat(coin).format((TextView) view.findViewById(R.id.trend));
        ((ImageView) view.findViewById(R.id.trend_icon)).setImageResource(IconHelper.getTrendIconResId(coin));

        view.findViewById(R.id.popup_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(getContext(), view);
                popup.inflate(R.menu.coin_card);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.action_settings) {
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        return view;
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
        void onLineChartKindChanged(String kind);
    }
}
