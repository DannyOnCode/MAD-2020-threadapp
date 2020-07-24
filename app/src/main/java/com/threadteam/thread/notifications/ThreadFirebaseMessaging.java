package com.threadteam.thread.notifications;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.threadteam.thread.R;
import com.threadteam.thread.activities.ViewServersActivity;

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
        }

        if (remoteMessage.getNotification() != null){
            String title = remoteMessage.getNotification().getTitle();
            String message = remoteMessage.getNotification().getBody();

            Log.d(TAG,"Message Notification Title: " + title);
            Log.d(TAG,"Message Notification Body: " + message);

            //Only send notifications if app is not in foreground
            if(!isAppForeground( this)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOreoNotification(title,message);
                } else {
                    sendNotification(title,message);
                }
            }

        }
    }

    /**
     * This function sends the notification to user's device if it is running Android 8/Oreo and above
     * @param title
     * @param messageBody
     */
    private void sendOreoNotification(String title, String messageBody){
        Intent intent = new Intent(this, ViewServersActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, messageBody, pendingIntent,
                defaultSound, R.mipmap.thread_t);

        oreoNotification.getManager().notify(0, builder.build());

    }

    /** This function sends the notification to user's device if it is running  Android 7/Nougat and below
     * @param title
     * @param messageBody
     * */
    private void sendNotification(String title, String messageBody) {

        Intent intent = new Intent(this, ViewServersActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.thread_t)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        NotificationManager noti = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        noti.notify(0, builder.build());
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
}
