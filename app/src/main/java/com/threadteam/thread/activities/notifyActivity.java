package com.threadteam.thread.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.threadteam.thread.R;

public class notifyActivity extends AppCompatActivity {
    private Button _Notify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificationtest);
        createNotificationChannel();

        _Notify = findViewById(R.id.notifyBtn);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "threadChat")
                .setSmallIcon(R.drawable.ic_baseline_chat_24)
                .setContentTitle("Thread")
                .setContentText("New Message")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("New Message"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        _Notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationManager.notify(100, builder.build());
            }
        });

    }

    private void createNotificationChannel(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "chatChannel";
            String description = "Channel for chat notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("threadChat",name,importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }
}