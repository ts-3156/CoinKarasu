package com.example.coinkarasu.data;

import com.example.coinkarasu.activities.HomeTabFragment.Kind;
import com.example.coinkarasu.coins.Coin;

import java.util.ArrayList;

public class Trending extends TrendingBase {

    public Trending(ArrayList<Coin> coins, Kind kind) {
        super(coins, kind);
    }
}
