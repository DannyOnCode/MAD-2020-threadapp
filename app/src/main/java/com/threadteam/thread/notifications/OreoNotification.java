package com.threadteam.thread.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

/**
 * This notifications class handles the notifications of user with devices which are Android version 8/Oreo & above.
 *
 * @author Mohamed Thabith
 * @version 2.0
 * @since 2.0
 */

public class OreoNotification extends ContextWrapper {

    private static final String CHANNEL_ID = "com.threadteam.thread";
    private static final String CHANNEL_NAME = "thread";

    private NotificationManager notificationManager;

    public OreoNotification(Context base) {
        super(base);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel();
        }
    }

    /**
     * This function creates the channel and sets channel settings for notifications
     */
    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(false);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel);
    }

    /**
     * This function returns Notification Manager
     * @return notificationManager
     */

    public NotificationManager getManager(){
        if (notificationManager == null){
            notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return  notificationManager;
    }

    /**
     * This function builds the notification
     * @param title
     * @param body
     * @param pendingIntent
     * @param soundUri
     * @param icon
     */

    @TargetApi(Build.VERSION_CODES.O)
    public  Notification.Builder getOreoNotification(String title, String body,
                                                     PendingIntent pendingIntent, Uri soundUri, int icon){
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(icon)
                .setSound(soundUri)
                .setAutoCancel(true);
    }
}