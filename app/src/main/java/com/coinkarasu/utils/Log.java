package com.coinkarasu.utils;

import android.app.Activity;
import android.os.Handler;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.Queue;

public class Log {
    private static final boolean DEBUG = true;

    private Activity activity;
    private Queue<String> queue;
    private boolean isRunning;

    public Log(Activity activity) {
        this.activity = activity;
        queue = new LinkedList<>();
        isRunning = false;
    }

    public void d(String tag, String message) {
        if (!DEBUG) return;
        android.util.Log.d(tag, message);
        makeToast(message);
    }

    public void i(String tag, String message) {
        if (!DEBUG) return;
        android.util.Log.i(tag, message);
        makeToast(message);
    }

    public void w(String tag, String message) {
        if (!DEBUG) return;
        android.util.Log.w(tag, message);
        makeToast(message);
    }

    public void e(String tag, String message) {
        if (!DEBUG) return;
        android.util.Log.e(tag, message);
        makeToast(message);
    }

    void makeToast(String message) {
        if (activity == null) {
            return;
        }

        queue.offer(message);
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

        final Handler handler = new Handler();

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!queue.isEmpty()) {
                    if (activity == null || activity.isFinishing()) {
                        activity = null;
                        break;
                    }

                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, queue.poll(), Toast.LENGTH_SHORT).show();
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
