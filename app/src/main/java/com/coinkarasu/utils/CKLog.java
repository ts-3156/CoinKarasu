package com.coinkarasu.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.R;
import com.coinkarasu.activities.MainActivity;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class CKLog {
    public static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "CKLog";
    private static final String TAG_PREFIX = "CK.";

    private enum Level {debug, info, warn, error}

    private static Context context;
    private static Queue<LogItem> queue;
    private static boolean isRunning;

    public static synchronized void setContext(Context context) {
        if (DEBUG) Log.d(TAG_PREFIX + TAG, "setContext()");
        if (BuildConfig.DEBUG && CKLog.context == null) {
            CKLog.context = context.getApplicationContext();
            queue = new LinkedList<>();
            isRunning = false;
        }
    }

    public static void releaseContext() {
        if (DEBUG) Log.d(TAG_PREFIX + TAG, "releaseContext()");
        context = null;
    }

    public static void d(String tag, String message) {
        if (!DEBUG) return;
        Log.d(TAG_PREFIX + tag, message);
        makeToast(TAG_PREFIX + tag, message, Level.debug);
    }

    public static void i(String tag, String message) {
        if (!DEBUG) return;
        Log.i(TAG_PREFIX + tag, message);
        makeToast(TAG_PREFIX + tag, message, Level.info);
    }

    public static void w(String tag, String message) {
        if (!DEBUG) return;
        Log.w(TAG_PREFIX + tag, message);
        makeToast(TAG_PREFIX + tag, message, Level.warn);
    }

    public static void e(String tag, String message) {
        if (!DEBUG) return;
        Log.e(TAG_PREFIX + tag, message);
        makeToast(TAG_PREFIX + tag, message, Level.error);
    }

    public static void e(String tag, Exception ex) {
        Crashlytics.logException(ex);
        if (DEBUG) Log.e(TAG_PREFIX + tag, ex.getMessage(), ex);
    }

    public static void e(String tag, String message, Exception ex) {
        Crashlytics.logException(ex);
        if (DEBUG) Log.e(TAG_PREFIX + tag, message, ex);
    }

    private synchronized static void makeToast(String tag, String message, Level level) {
        if (context == null || !BuildConfig.DEBUG || !PrefHelper.isDebugToastEnabled(context)) {
            return;
        }

        queue.offer(new LogItem(CKDateUtils.now(), tag, message, level));

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

                    List<LogItem> items = new ArrayList<>();
                    while (!queue.isEmpty() && items.size() <= 5) {
                        LogItem item = queue.poll();
                        if (item == null || item.level.ordinal() < confLevel.ordinal()) {
                            continue;
                        }
                        items.add(item);
                    }

                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < items.size(); i++) {
                        if (i != 0) {
                            builder.append("\n\n");
                        }
                        LogItem item = items.get(i);
                        builder.append(makeText(item));
                        sendNotification(item);
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
        return getTime(item.time) + " " + item.tag + "\n" + item.message.substring(0, Math.min(item.message.length(), 100));
    }

    private static void sendNotification(LogItem item) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = context.getString(R.string.debug_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.ic_notif)
                        .setContentTitle(item.tag)
                        .setContentText(item.message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setTicker(item.message);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(channelId) == null) {
                String channelName = context.getString(R.string.debug_notification_channel_name);
                NotificationChannel channel =
                        new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
            }
        }

        manager.notify(new Random().nextInt(100000), builder.build());
    }

    private static String getTime(long time) {
        return DateUtils.getRelativeTimeSpanString(time, CKDateUtils.now(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL).toString();
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
