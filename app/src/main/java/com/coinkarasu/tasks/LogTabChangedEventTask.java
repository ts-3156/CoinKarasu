package com.coinkarasu.tasks;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;

import com.coinkarasu.activities.MainActivity;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.database.AppDatabase;
import com.coinkarasu.database.LaunchEvent;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.PrefHelper;
import com.coinkarasu.utils.UuidUtils;
import com.google.firebase.analytics.FirebaseAnalytics;

public class LogTabChangedEventTask extends AsyncTask<Object, Void, Void> {
    private static final boolean DEBUG = true;
    private static final String TAG = "LogTabChangedEventTask";

    @Override
    protected Void doInBackground(Object... params) {
        try {
            MainActivity activity = (MainActivity) params[0];
            NavigationKind kind = (NavigationKind) params[1];

            FirebaseAnalytics analytics = activity.getFirebaseAnalytics();
            LaunchEvent launchEvent = AppDatabase.getAppDatabase(activity).launchEventDao().first();

            if (analytics != null) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, kind.name());
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, kind.name());
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "NavigationKind");
                if (launchEvent != null) {
                    bundle.putString(FirebaseAnalytics.Param.START_DATE, DateFormat.format("yyyy-MM-dd", launchEvent.getCreated()).toString());
                }
                if (UuidUtils.exists(activity)) {
                    bundle.putString("uuid", UuidUtils.get(activity));
                }
                bundle.putString("sync_interval", String.valueOf(PrefHelper.getSyncInterval(activity)));
                bundle.putString("is_premium", String.valueOf(PrefHelper.isPremium(activity)));
                analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                if (DEBUG) CKLog.d(TAG, bundle.toString());
            }
        } catch (Exception e) {
            CKLog.e(TAG, e);
        }

        return null;
    }
}
