package com.coinkarasu.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

@Dao
public interface ViewCoinEventDao {

    @Query("SELECT * FROM view_coin_events ORDER BY created DESC LIMIT 1")
    ViewCoinEvent last();

    @Query("SELECT COUNT(*) from view_coin_events")
    int size();

    @Insert
    void insertEvent(ViewCoinEvent event);
}
