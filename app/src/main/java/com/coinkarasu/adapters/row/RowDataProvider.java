package com.coinkarasu.adapters.row;

import com.coinkarasu.coins.Coin;

public interface RowDataProvider {
    Coin getItem(int position);
}

