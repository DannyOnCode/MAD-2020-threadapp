package com.threadteam.thread.libraries;

import android.widget.Adapter;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.threadteam.thread.LogHandler;

/**
 * Wrapper for general notifications methods.
 * These should be available application wide.
 *
 * @author Mohamed Thabith
 * @version 2.0
 * @since 2.0
 */

public class Notifications {

    /**
     * Subscribe user to all notifications for all servers user is in .
     * @param logHandler The LogHandler for the current activity.
     * @param databaseRef The current DatabaseReference for the activity.
     */

    public static void subscribeAllNotifications(LogHandler logHandler,DatabaseReference databaseRef){
        subscribeMsgNotification(logHandler,databaseRef);
        subscribePostsNotification(logHandler,databaseRef);
        subscribeSystemNotification(logHandler,databaseRef);
    }

    /**
     * Subscribe user to only message notifications for all servers user is in .
     * @param logHandler The LogHandler for the current activity.
     * @param databaseRef The current DatabaseReference for the activity.
     */

    public static void subscribeMsgNotification(final LogHandler logHandler, DatabaseReference databaseRef){
        logHandler.printLogWithMessage("subscribeMsgNotification invoked");

        // getServers:              ATTEMPTS TO GET SERVERS USER IS IN
        //                          CORRECT INVOCATION CODE: databaseRef.child("users")
        //                                                                  .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
        //                                                                  .child("_subscribedServers")
        //                                                                  .addValueEventListener(getServers);

        ValueEventListener getServers = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    String server = (String) data.getKey();
                    if (server == null){
                        logHandler.printDatabaseResultLog(".getValue()", "server", "getServers", "null");
                        return;
                    }
                    final String topic = server;

                    FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                logHandler.printLogWithMessage("SUBSCRIBED TO /topics/" + topic +" SUCCESSFULLY");

                            }
                            else{
                                logHandler.printLogWithMessage("COULD NOT SUBSCRIBE TO: " + topic);
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

    /**
     * Subscribe user to only system notifications for all servers user is in .
     * @param logHandler The LogHandler for the current activity.
     * @param databaseRef The current DatabaseReference for the activity.
     */

    public static void subscribeSystemNotification(final LogHandler logHandler, DatabaseReference databaseRef){

        // getServers:              ATTEMPTS TO GET SERVERS USER IS IN
        //                          CORRECT INVOCATION CODE: databaseRef.child("users")
        //                                                                  .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
        //                                                                  .child("_subscribedServers")
        //                                                                  .addValueEventListener(getServers);

        ValueEventListener getServers = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    String server = (String) data.getKey();
                    if (server == null){
                        logHandler.printDatabaseResultLog(".getValue()", "server", "getServers", "null");
                        return;
                    }
                    final String topic = "system" + server;

                    FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                logHandler.printLogWithMessage("SUBSCRIBED TO /topics/" + topic + " SUCCESSFULLY");

                            } else {
                                logHandler.printLogWithMessage("COULD NOT SUBSCRIBE TO: " + topic);
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

    /**
     * Subscribe user to only posts notifications for all servers user is in .
     * @param logHandler The LogHandler for the current activity.
     * @param databaseRef The current DatabaseReference for the activity.
     */

    public static void subscribePostsNotification(final LogHandler logHandler, DatabaseReference databaseRef){

        // getServers:              ATTEMPTS TO GET SERVERS USER IS IN
        //                          CORRECT INVOCATION CODE: databaseRef.child("users")
        //                                                                  .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
        //                                                                  .child("_subscribedServers")
        //                                                                  .addValueEventListener(getServers);

        ValueEventListener getServers = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    String server = (String) data.getKey();
                    if (server == null){
                        logHandler.printDatabaseResultLog(".getValue()", "server", "getServers", "null");
                        return;
                    }
                    final String topic = "posts" + server;

                    FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                logHandler.printLogWithMessage("SUBSCRIBED TO /topics/" + topic +" SUCCESSFULLY");

                            }
                            else{
                                logHandler.printLogWithMessage("COULD NOT SUBSCRIBE TO: " + topic);
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

    /**
     * Unsubscribe user from all notifications for all servers user is in .
     * @param logHandler The LogHandler for the current activity.
     * @param databaseRef The current DatabaseReference for the activity.
     */

    public static void unsubscribeAllNotifications(LogHandler logHandler,DatabaseReference databaseRef){
        unsubscribeMsgNotification(logHandler,databaseRef);
        unsubscribePostsNotification(logHandler,databaseRef);
        unsubscribeSystemNotification(logHandler,databaseRef);
    }

    /**
     * Unsubscribe user from only message notifications for all servers user is in .
     * @param logHandler The LogHandler for the current activity.
     * @param databaseRef The current DatabaseReference for the activity.
     */

    public static void unsubscribeMsgNotification(final LogHandler logHandler, DatabaseReference databaseRef){

        // getServers:              ATTEMPTS TO GET SERVERS USER IS IN
        //                          CORRECT INVOCATION CODE: databaseRef.child("users")
        //                                                                  .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
        //                                                                  .child("_subscribedServers")
        //                                                                  .addValueEventListener(getServers);

        ValueEventListener getServers = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    String server = (String) data.getKey();
                    if (server == null){
                        logHandler.printDatabaseResultLog(".getValue()", "server", "getServers", "null");
                        return;
                    }
                    final String topic = server;

                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                logHandler.printLogWithMessage("UNSUBSCRIBED FROM /topics/" + topic +" SUCCESSFULLY");

                            }
                            else{
                                logHandler.printLogWithMessage("COULD NOT UNSUBSCRIBE FROM: " + topic);
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

    /**
     * Unsubscribe user from only system notifications for all servers user is in .
     * @param logHandler The LogHandler for the current activity.
     * @param databaseRef The current DatabaseReference for the activity.
     */

    public static void unsubscribeSystemNotification(final LogHandler logHandler, DatabaseReference databaseRef){

        // getServers:              ATTEMPTS TO GET SERVERS USER IS IN
        //                          CORRECT INVOCATION CODE: databaseRef.child("users")
        //                                                                  .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
        //                                                                  .child("_subscribedServers")
        //                                                                  .addValueEventListener(getServers);

        ValueEventListener getServers = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    String server = (String) data.getKey();
                    if (server == null){
                        logHandler.printDatabaseResultLog(".getValue()", "server", "getServers", "null");
                        return;
                    }
                    final String topic = "system" + server;

                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                logHandler.printLogWithMessage("UNSUBSCRIBED FROM /topics/" + topic +" SUCCESSFULLY");

                            }
                            else{
                                logHandler.printLogWithMessage("COULD NOT UNSUBSCRIBE FROM: " + topic);
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

    /**
     * Unsubscribe user from only posts notifications for all servers user is in .
     * @param logHandler The LogHandler for the current activity.
     * @param databaseRef The current DatabaseReference for the activity.
     */

    public static void unsubscribePostsNotification(final LogHandler logHandler, DatabaseReference databaseRef){

        // getServers:              ATTEMPTS TO GET SERVERS USER IS IN
        //                          CORRECT INVOCATION CODE: databaseRef.child("users")
        //                                                                  .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
        //                                                                  .child("_subscribedServers")
        //                                                                  .addValueEventListener(getServers);

        ValueEventListener getServers = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    String server = (String) data.getKey();
                    if (server == null){
                        logHandler.printDatabaseResultLog(".getValue()", "server", "getServers", "null");
                        return;
                    }
                    final String topic = "posts" + server;

                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                logHandler.printLogWithMessage("UNSUBSCRIBED FROM /topics/" + topic +" SUCCESSFULLY");

                            }
                            else{
                                logHandler.printLogWithMessage("COULD NOT UNSUBSCRIBE FROM: " + topic);
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
