package com.example.coinkarasu.activities;

import android.view.View;

import com.example.coinkarasu.coins.Coin;

public interface OnItemClickListener {
    void onItemClick(Coin coin, View view, int position);
}
