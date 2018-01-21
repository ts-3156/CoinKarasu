package com.coinkarasu.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.coinkarasu.activities.settings.DebugPreferencesFragment;
import com.coinkarasu.database.AppDatabase;
import com.coinkarasu.database.LaunchEvent;

import java.util.Date;

public class GetFirstLaunchDateTask extends AsyncTask<Context, Void, Date> {
    private Callback callback;

    public GetFirstLaunchDateTask(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected Date doInBackground(Context... params) {
        AppDatabase db = AppDatabase.getAppDatabase(params[0]);
        LaunchEvent event = db.launchEventDao().first();
        return (event == null ? null : event.getCreated());
    }

    @Override
    protected void onPostExecute(Date result) {
        if (callback != null) {
            callback.run(result);
        }
        callback = null;
    }

    public interface Callback {
        void run(Date date);
    }
}
