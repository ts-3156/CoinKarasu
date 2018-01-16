package com.coinkarasu.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.R;
import com.coinkarasu.activities.MainActivity;
import com.crashlytics.android.Crashlytics;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class CKLog {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "CKLog";

    private enum Level {debug, info, warn, error}

    private static Context context;
    private static Queue<LogItem> queue;
    private static boolean isRunning;

    public static synchronized void setContext(Context context) {
        Log.d(TAG, "setContext()");
        if (CKLog.context == null) {
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
        makeToast(tag, message, Level.debug);
    }

    public static void i(String tag, String message) {
        if (!DEBUG) return;
        Log.i(tag, message);
        makeToast(tag, message, Level.info);
    }

    public static void w(String tag, String message) {
        if (!DEBUG) return;
        Log.w(tag, message);
        makeToast(tag, message, Level.warn);
    }

    public static void e(String tag, String message) {
        if (!DEBUG) return;
        Log.e(tag, message);
        makeToast(tag, message, Level.error);
    }

    public static void e(String tag, Exception ex) {
        Crashlytics.logException(ex);
        if (DEBUG) Log.e(tag, ex.getMessage());
    }

    public static void e(String tag, String message, Exception ex) {
        Crashlytics.logException(ex);
        if (DEBUG) Log.e(tag, message, ex);
    }

    private synchronized static void makeToast(String tag, String message, Level level) {
        if (context == null || !BuildConfig.DEBUG || !PrefHelper.isDebugToastEnabled(context)) {
            return;
        }

        queue.offer(new LogItem(System.currentTimeMillis(), tag, message, level));

        if (isRunning) {
            return;
        }
        isRunning = true;

        final Handler handler = new Handler(context.getMainLooper());

        final Thread thread = new Thread() {
            @Override
            public void run() {
                while (!queue.isEmpty()) {
                    if (context == null) {
                        isRunning = false;
                        break;
                    }

                    String levelString = PrefHelper.getDebugToastLevel(context);
                    if (TextUtils.isEmpty(levelString)) {
                        isRunning = false;
                        break;
                    }
                    Level confLevel = Level.valueOf(levelString);

                    StringBuilder builder = new StringBuilder();
                    int pollCount = 0;

                    while (!queue.isEmpty() && pollCount <= 5) {
                        LogItem item = queue.poll();
                        if (item == null || item.level.ordinal() < confLevel.ordinal()) {
                            continue;
                        }

                        if (pollCount != 0) {
                            builder.append("\n\n");
                        }
                        builder.append(makeText(item));
                        pollCount++;
                    }

                    final String str = builder.toString();
                    if (!TextUtils.isEmpty(str)) {
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

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

    private static String makeText(LogItem item) {
        if (item == null) {
            return "";
        }
        sendNotification(item);
        return getTime(item.time) + " " + item.tag + "\n" + item.message.substring(0, Math.min(item.message.length(), 100));
    }

    private static void sendNotification(LogItem item) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, "debug")
                        .setSmallIcon(R.mipmap.ic_launcher_karasu)
                        .setContentTitle(item.tag)
                        .setContentText(item.message)
                        .setTicker(item.message);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(new Intent(context, MainActivity.class));

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel("debug") == null) {
                NotificationChannel channel = new
                        NotificationChannel("debug", "Debug", NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
            }
        }
        manager.notify(new Random().nextInt(100000), builder.build());
    }

    private static String getTime(long time) {
        return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL).toString();
    }

    private static class LogItem {
        long time;
        String tag;
        String message;
        Level level;

        public LogItem(long time, String tag, String message, Level level) {
            this.time = time;
            this.tag = tag;
            this.message = message;
            this.level = level;
        }
    }
}
