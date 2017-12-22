package com.example.coinkarasu.api.cryptocompare.data;

import com.example.coinkarasu.coins.PriceMultiFullCoin;

import java.util.ArrayList;

public interface Toplist {
    ArrayList<PriceMultiFullCoin> getCoins();
}
