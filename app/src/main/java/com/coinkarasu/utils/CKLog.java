package com.coinkarasu.utils;

import android.app.Activity;
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

    private Context context;
    private Queue<String> queue;
    private boolean isRunning;

    public CKLog(Context context) {
//        this.context = context.getApplicationContext();
        queue = new LinkedList<>();
        isRunning = false;
    }

    public static void d(String tag, String message) {
        if (!DEBUG) return;
        Log.d(tag, message);
//        makeToast(tag, message);
    }

    public static void i(String tag, String message) {
        if (!DEBUG) return;
        Log.i(tag, message);
//        makeToast(tag, message);
    }

    public static void w(String tag, String message) {
        if (!DEBUG) return;
        Log.w(tag, message);
//        makeToast(tag, message);
    }

    public static void e(String tag, String message) {
        if (!DEBUG) return;
        Log.e(tag, message);
//        makeToast(tag, message);
    }

    public static void e(String tag, Exception ex) {
        Crashlytics.logException(ex);
        if (DEBUG) Log.e(tag, ex.getMessage());
    }

    public static void e(String tag, String message, Exception ex) {
        Crashlytics.logException(ex);
        if (DEBUG) Log.e(tag, message, ex);
    }

    void makeToast(String tag, String message) {
        if (context == null) {
            return;
        }

        queue.offer(tag + ": " + message);
        if (isRunning) {
            return;
        }
        isRunning = true;

        final Listener listener = new Listener() {
            @Override
            public void finished() {
                isRunning = false;
            }
        };

        final Handler handler = new Handler(context.getMainLooper());

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!queue.isEmpty()) {
                    if (context == null) {
                        break;
                    }

                    if (context instanceof Activity && ((Activity) context).isFinishing()) {
                        context = null;
                        break;
                    }

                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(context, queue.poll(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    try {
                        Thread.sleep(2000); // LENGTH_SHORT
                    } catch (Exception e) {
                        break;
                    }
                }

                listener.finished();
            }
        };
        thread.start();
    }

    private interface Listener {
        void finished();
    }
}
