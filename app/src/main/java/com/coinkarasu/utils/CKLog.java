package com.coinkarasu.utils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.coinkarasu.BuildConfig;
import com.crashlytics.android.Crashlytics;

import java.util.LinkedList;
import java.util.Queue;

public class CKLog {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "CKLog";

    private static Context context;
    private static Queue<String> queue;
    private static boolean isRunning;

    public static synchronized void setContext(Context context) {
        if (CKLog.context == null) {
            Log.d(TAG, "setContext()");
            CKLog.context = context.getApplicationContext();
            queue = new LinkedList<>();
            isRunning = false;
        }
    }

    public static void releaseContext() {
        Log.d(TAG, "releaseContext()");
        context = null;
    }

    public static void d(String tag, String message) {
        if (!DEBUG) return;
        Log.d(tag, message);
        makeToast(tag, message);
    }

    public static void i(String tag, String message) {
        if (!DEBUG) return;
        Log.i(tag, message);
        makeToast(tag, message);
    }

    public static void w(String tag, String message) {
        if (!DEBUG) return;
        Log.w(tag, message);
        makeToast(tag, message);
    }

    public static void e(String tag, String message) {
        if (!DEBUG) return;
        Log.e(tag, message);
        makeToast(tag, message);
    }

    public static void e(String tag, Exception ex) {
        Crashlytics.logException(ex);
        if (DEBUG) Log.e(tag, ex.getMessage());
    }

    public static void e(String tag, String message, Exception ex) {
        Crashlytics.logException(ex);
        if (DEBUG) Log.e(tag, message, ex);
    }

    private static void makeToast(String tag, String message) {
        if (context == null || !BuildConfig.DEBUG || !PrefHelper.isDebugToastEnabled(context)) {
            return;
        }

        queue.offer(tag + ": " + message);

        if (isRunning) {
            return;
        }
        isRunning = true;

        final Handler handler = new Handler(context.getMainLooper());

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!queue.isEmpty()) {
                    if (context == null) {
                        isRunning = false;
                        break;
                    }

                    handler.post(new Runnable() {
                        public void run() {
                            if (context != null) {
                                Toast.makeText(context, queue.poll(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    try {
                        Thread.sleep(2000); // LENGTH_SHORT
                    } catch (Exception e) {
                        break;
                    }
                }

                isRunning = false;
            }
        };
        thread.start();
    }
}
