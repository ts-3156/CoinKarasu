package com.coinkarasu.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface CoinListCoinDao {

    @Query("SELECT * FROM coin_list_coins")
    List<CoinListCoin> findAll();

    @Query("SELECT * FROM coin_list_coins where symbol = :symbol")
    CoinListCoin findBySymbol(String symbol);

    @Query("SELECT * FROM coin_list_coins where symbol IN (:symbols)")
    List<CoinListCoin> findBySymbols(String[] symbols);

    @Query("SELECT COUNT(*) from coin_list_coins")
    int size();

    @Insert
    void insertCoins(List<CoinListCoin> coins);

    @Query("DELETE FROM coin_list_coins")
    void deleteAll();
}