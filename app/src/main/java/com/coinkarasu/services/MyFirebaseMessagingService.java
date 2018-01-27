package com.coinkarasu.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.coinkarasu.R;
import com.coinkarasu.activities.MainActivity;
import com.coinkarasu.utils.CKLog;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "MyFirebaseMessagingService";

    /**
     * アプリの状態     通知              データ             両方
     * フォアグラウンド onMessageReceived onMessageReceived onMessageReceived
     * バックグラウンド システムトレイ      onMessageReceived 通知: システムトレイ、データ: インテントの追加部分にあるデータ
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (DEBUG) CKLog.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            if (DEBUG) CKLog.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleNow();
        }

        if (remoteMessage.getNotification() != null) {
            if (DEBUG) CKLog.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        sendNotification("message body");
    }

    private void handleNow() {
        if (DEBUG) CKLog.d(TAG, "Short lived task is done.");
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notif)
                        .setContentTitle("FCM Message")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setTicker(messageBody);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(channelId) == null) {
                String channelName = getString(R.string.default_notification_channel_name);
                NotificationChannel channel =
                        new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
            }
        }

        manager.notify(new Random().nextInt(100000), builder.build());
    }
}