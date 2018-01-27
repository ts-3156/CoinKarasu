package com.coinkarasu.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {CoinListCoin.class, LaunchEvent.class, ViewCoinEvent.class}, version = 6, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract CoinListCoinDao coinListCoinDao();

    public abstract LaunchEventDao launchEventDao();

    public abstract ViewCoinEventDao viewCoinEventDao();

    public static synchronized AppDatabase getAppDatabase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "coin-karasu-database")
                    // .allowMainThreadQueries()
                    // .fallbackToDestructiveMigration()
                    .build();
        }

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }
}
