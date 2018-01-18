package com.coinkarasu.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.Date;
import java.util.List;

@Dao
public interface LaunchEventDao {

    @Query("SELECT * FROM launch_events ORDER BY created LIMIT 1")
    LaunchEvent first();

    @Query("SELECT * FROM launch_events where created BETWEEN :from AND :to")
    List<LaunchEvent> findByCreated(Date from, Date to);

    @Query("SELECT COUNT(*) from launch_events")
    int size();

    @Insert
    void insertEvent(LaunchEvent event);

    @Query("DELETE FROM launch_events")
    void deleteAll();
}