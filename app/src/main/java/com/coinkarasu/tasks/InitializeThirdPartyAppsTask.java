package com.coinkarasu.tasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

import io.fabric.sdk.android.Fabric;

public class InitializeThirdPartyAppsTask extends AsyncTask<Activity, Void, Void> {
    private static final boolean DEBUG = true;
    private static final String TAG = "InitializeThirdPartyAppsTask";

    protected Runnable runnable;

    public InitializeThirdPartyAppsTask() {
        this(null);
    }

    public InitializeThirdPartyAppsTask(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    protected Void doInBackground(Activity... params) {
        Activity activity = params[0];
        Fabric.with(activity, new Crashlytics());
        ((FirebaseAnalyticsReceiver) activity).setFirebaseAnalytics(FirebaseAnalytics.getInstance(activity));

        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        if (runnable != null) {
            runnable.run();
        }
        runnable = null;
    }

    public interface FirebaseAnalyticsReceiver {
        void setFirebaseAnalytics(FirebaseAnalytics firebaseAnalytics);
    }
}
