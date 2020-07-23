package com.threadteam.thread.libraries;

import android.widget.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.threadteam.thread.models.Server;
import com.threadteam.thread.LogHandler;

import javax.xml.validation.Validator;

public class Notifications {

    public static void subscribeAllNotifications(LogHandler logHandler,DatabaseReference databaseRef){
        subscribeMsgNotification(logHandler,databaseRef);
        subscribePostsNotification(logHandler,databaseRef);
        subscribeSystemNotification(logHandler,databaseRef);
    }


    public static void subscribeMsgNotification(final LogHandler logHandler, DatabaseReference databaseRef){
        logHandler.printLogWithMessage("subscribeMsgNotification invoked");

        ValueEventListener getServers = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    String server = (String) data.getKey();
                    final String topic = server;

                    logHandler.printLogWithMessage("topic: " + topic);


                    FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                logHandler.printLogWithMessage("SUBSCRIBED TO /topics/" + topic +" SUCCESSFULLY");

                            }
                            else{
                                logHandler.printLogWithMessage("COULD NOT SUBSCRIBE");
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        databaseRef.child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("_subscribedServers")
                .addValueEventListener(getServers);
    }

    public static void subscribeSystemNotification(final LogHandler logHandler, DatabaseReference databaseRef){

        ValueEventListener getServers = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    String server = (String) data.getKey();
                    final String topic = "system" + server;

                    logHandler.printLogWithMessage("topic: " + topic);


                    FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                logHandler.printLogWithMessage("SUBSCRIBED TO /topics/" + topic + " SUCCESSFULLY");

                            } else {
                                logHandler.printLogWithMessage("COULD NOT SUBSCRIBE");
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        databaseRef.child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("_subscribedServers")
                .addValueEventListener(getServers);

    }

    public static void subscribePostsNotification(final LogHandler logHandler, DatabaseReference databaseRef){

        ValueEventListener getServers = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    String server = (String) data.getKey();
                    final String topic = "posts" + server;

                    logHandler.printLogWithMessage("topic: " + topic);


                    FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                logHandler.printLogWithMessage("SUBSCRIBED TO /topics/" + topic +" SUCCESSFULLY");

                            }
                            else{
                                logHandler.printLogWithMessage("COULD NOT SUBSCRIBE");
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseRef.child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("_subscribedServers")
                .addValueEventListener(getServers);

    }

    public static void unsubscribeAllNotifications(LogHandler logHandler,DatabaseReference databaseRef){
        unsubscribeMsgNotification(logHandler,databaseRef);
        unsubscribePostsNotification(logHandler,databaseRef);
        unsubscribeSystemNotification(logHandler,databaseRef);
    }

    public static void unsubscribeMsgNotification(final LogHandler logHandler, DatabaseReference databaseRef){

        ValueEventListener getServers = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    String server = (String) data.getKey();
                    final String topic = server;

                    logHandler.printLogWithMessage("topic: " + topic);


                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                logHandler.printLogWithMessage("UNSUBSCRIBED FROM /topics/" + topic +" SUCCESSFULLY");

                            }
                            else{
                                logHandler.printLogWithMessage("COULD NOT UNSUBSCRIBE");
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseRef.child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("_subscribedServers")
                .addValueEventListener(getServers);
    }

    public static void unsubscribeSystemNotification(final LogHandler logHandler, DatabaseReference databaseRef){

        ValueEventListener getServers = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    String server = (String) data.getKey();
                    final String topic = "system" + server;

                    logHandler.printLogWithMessage("topic: " + topic);


                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                logHandler.printLogWithMessage("UNSUBSCRIBED FROM /topics/" + topic +" SUCCESSFULLY");

                            }
                            else{
                                logHandler.printLogWithMessage("COULD NOT UNSUBSCRIBE");
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseRef.child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("_subscribedServers")
                .addValueEventListener(getServers);

    }

    public static void unsubscribePostsNotification(final LogHandler logHandler, DatabaseReference databaseRef){

        ValueEventListener getServers = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    String server = (String) data.getKey();
                    final String topic = "posts" + server;

                    logHandler.printLogWithMessage("topic: " + topic);


                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                logHandler.printLogWithMessage("UNSUBSCRIBED FROM /topics/" + topic +" SUCCESSFULLY");

                            }
                            else{
                                logHandler.printLogWithMessage("COULD NOT UNSUBSCRIBE");
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseRef.child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("_subscribedServers")
                .addValueEventListener(getServers);
    }
}
