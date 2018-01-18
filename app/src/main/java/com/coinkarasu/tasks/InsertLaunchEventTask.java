package com.coinkarasu.tasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.coinkarasu.database.AppDatabase;
import com.coinkarasu.database.LaunchEvent;

import java.util.Date;

public class InsertLaunchEventTask extends AsyncTask<Activity, Void, Void> {
    @Override
    protected Void doInBackground(Activity... params) {
        Activity activity = params[0];
        AppDatabase db = AppDatabase.getAppDatabase(activity);
        db.launchEventDao().insertEvent(new LaunchEvent(activity.getClass().getSimpleName(), new Date()));

        return null;
    }
}
