package com.coinkarasu.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.coinkarasu.activities.MainActivity;
import com.coinkarasu.database.AppDatabase;
import com.coinkarasu.database.LaunchEvent;
import com.coinkarasu.database.LaunchEventDao;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class InsertDummyFirstLaunchDateTask extends AsyncTask<Context, Void, Date> {
    private Callback callback;

    public InsertDummyFirstLaunchDateTask(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected Date doInBackground(Context... params) {
        AppDatabase db = AppDatabase.getAppDatabase(params[0]);
        LaunchEventDao dao = db.launchEventDao();

        Date dummy = new Date(dao.first().getCreated().getTime() - TimeUnit.DAYS.toMillis(3));
        dao.insertEvent(new LaunchEvent(MainActivity.class.getSimpleName(), dummy));

        return dao.first().getCreated();
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
