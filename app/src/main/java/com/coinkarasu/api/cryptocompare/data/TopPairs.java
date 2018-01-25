package com.coinkarasu.api.cryptocompare.data;

import com.coinkarasu.coins.TopPairCoin;

import java.util.List;

public interface TopPairs {

    List<TopPairCoin> getTopPairCoins();
}
