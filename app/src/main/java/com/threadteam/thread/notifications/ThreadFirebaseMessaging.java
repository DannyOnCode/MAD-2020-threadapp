package com.threadteam.thread.notifications;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.threadteam.thread.R;
import com.threadteam.thread.activities.ChatActivity;
import com.threadteam.thread.activities.PostsActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * This notifications class handles the receiving of content from the Firebase Cloud messaging API and displaying to the user.
 *
 * @author Mohamed Thabith
 * @version 2.0
 * @since 2.0
 */


public class ThreadFirebaseMessaging extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMsgService";
    /** Retrieves the content from the Firebase Cloud messaging API*/
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        //Check if message contains a data payload
        if (remoteMessage.getData().size() > 0){
            Log.d(TAG,"Message data payload: " + remoteMessage.getData());



            //Only send notifications if app is not in foreground
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOreoNotification(remoteMessage);
                } else {
                    sendNotification(remoteMessage);
                }

        }
    }

    /**
     * This function sends the notification to user's device if it is running Android 8/Oreo and above
     * @param remoteMessage
     */
    private void sendOreoNotification(RemoteMessage remoteMessage){
        String serverId = remoteMessage.getData().get("serverID");
        SharedPreferences sharedPreferences = getSharedPreferences("ChatActivity",MODE_PRIVATE);
        String server = sharedPreferences.getString("server","");
        Log.d(TAG,"IsSERVER? :" + server );

        if(!server.equals(serverId)) {

            String title = remoteMessage.getData().get("title");
            String messageBody = remoteMessage.getData().get("body");
            Bitmap bitmap = getBitmapFromURL(remoteMessage.getData().get("profile"));
            String ownerID = remoteMessage.getData().get("ownerID");
            String activity = remoteMessage.getData().get("activity");
            boolean isOwner = false;


            Log.d(TAG, "ownerID: " + ownerID);
            Log.d(TAG, "Current User: " + FirebaseAuth.getInstance().getCurrentUser().getUid());

            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(ownerID)) {
                isOwner = true;
                Log.d(TAG, "Current User is owner");
            }

            Intent goToActivity;
            PendingIntent pendingIntent = null;
            TaskStackBuilder stackBuilder;

            if (activity.equals("posts")) {
                goToActivity = new Intent(this, PostsActivity.class);
                stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addNextIntentWithParentStack(goToActivity);
                goToActivity.putExtra("SERVER_ID", serverId);
                goToActivity.putExtra("IS_OWNER", isOwner);
                pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            } else if (activity.equals("chats")) {
                goToActivity = new Intent(this, ChatActivity.class);
                stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addNextIntentWithParentStack(goToActivity);
                goToActivity.putExtra("SERVER_ID", serverId);
                goToActivity.putExtra("IS_OWNER", isOwner);
                pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            }


            Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            OreoNotification oreoNotification = new OreoNotification(this);
            Notification.Builder builder = oreoNotification.getOreoNotification(title, messageBody, bitmap, pendingIntent,
                    defaultSound, R.drawable.thread_png);

            oreoNotification.getManager().notify(0, builder.build());
        }
    }

    /** This function sends the notification to user's device if it is running  Android 7/Nougat and below
     * @param remoteMessage
     * */
    private void sendNotification(RemoteMessage remoteMessage) {
        String serverId = remoteMessage.getData().get("serverID");
        SharedPreferences sharedPreferences = getSharedPreferences("ChatActivity",MODE_PRIVATE);
        String server = sharedPreferences.getString("server","");
        Log.d(TAG,"IsSERVER? :" + server );

        if(!server.equals(serverId)) {


            String title = remoteMessage.getData().get("title");
            String messageBody = remoteMessage.getData().get("body");
            Bitmap bitmap = getBitmapFromURL(remoteMessage.getData().get("profile"));
            String ownerID = remoteMessage.getData().get("ownerID");
            String activity = remoteMessage.getData().get("activity");
            boolean isOwner = false;

            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(ownerID)) {
                isOwner = true;
            }

            Intent goToActivity;
            PendingIntent pendingIntent = null;
            TaskStackBuilder stackBuilder;

            if (activity.equals("posts")) {
                goToActivity = new Intent(this, PostsActivity.class);
                stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addNextIntentWithParentStack(goToActivity);
                goToActivity.putExtra("SERVER_ID", serverId);
                goToActivity.putExtra("IS_OWNER", isOwner);
                pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            } else if (activity.equals("chats")) {
                goToActivity = new Intent(this, ChatActivity.class);
                stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addNextIntentWithParentStack(goToActivity);
                goToActivity.putExtra("SERVER_ID", serverId);
                goToActivity.putExtra("IS_OWNER", isOwner);
                pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.thread_png)
                    .setLargeIcon(bitmap)
                    .setContentTitle(title)
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(defaultSound)
                    .setContentIntent(pendingIntent)
                    .setPriority(Notification.PRIORITY_HIGH);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0, builder.build());
        }
    }

    /** This function checks if the App is in foreground of user's device
     * @param context
     * */
    public static boolean isAppForeground(Context context) {

        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> l = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : l) {
            if (info.uid == context.getApplicationInfo().uid && info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    /**
     *  This function converts a imageURL to bitmap
     * @param src
     * @return
     */
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
