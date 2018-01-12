package com.coinkarasu.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.google.firebase.analytics.FirebaseAnalytics;

public abstract class InitializeThirdPartyAppsTask extends AsyncTask<Void, Void, Void> {
    private static final boolean DEBUG = true;
    private static final String TAG = "InitializeThirdPartyAppsTask";

    protected Context context;
    protected FirebaseAnalyticsReceiver receiver;
    protected Runnable runnable;

    protected InitializeThirdPartyAppsTask(Context context, FirebaseAnalyticsReceiver receiver, Runnable runnable) {
        this.context = context;
        this.receiver = receiver;
        this.runnable = runnable;
    }

    @Override
    protected abstract Void doInBackground(Void... params);

    @Override
    protected void onPostExecute(Void v) {
        if (runnable != null) {
            runnable.run();
        }
        context = null;
        runnable = null;
    }

    public interface FirebaseAnalyticsReceiver {
        void setFirebaseAnalytics(FirebaseAnalytics firebaseAnalytics);
    }
}
